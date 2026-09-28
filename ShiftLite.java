import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class ShiftLite {
    private static final Path DATA_FILE = Path.of("shifts.tsv");
    private record Shift(String person, LocalDate date, LocalTime start, LocalTime end) {}
    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
    private static List<Shift> load() throws IOException {
        List<Shift> shifts = new ArrayList<>();
        if (!Files.exists(DATA_FILE)) return shifts;
        int lineNumber = 0;
        for (String line : Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) continue;
            String[] fields = line.split("\\|", -1);
            if (fields.length != 4) throw new IOException("Invalid data at line " + lineNumber + ".");
            shifts.add(new Shift(decode(fields[0]), LocalDate.parse(fields[1]),
                    LocalTime.parse(fields[2]), LocalTime.parse(fields[3])));
        }
        return shifts;
    }
    private static void save(List<Shift> shifts) throws IOException {
        List<String> lines = shifts.stream()
                .map(shift -> encode(shift.person()) + "|" + shift.date() + "|" + shift.start() + "|" + shift.end())
                .toList();
        Path temporary = DATA_FILE.resolveSibling("shifts.tsv.tmp");
        Files.write(temporary, lines, StandardCharsets.UTF_8);
        Files.move(temporary, DATA_FILE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    private static void usage() {
        System.out.println("Shift Lite - local team shift planner");
        System.out.println("  add <person> <yyyy-mm-dd> <HH:mm> <HH:mm>");
        System.out.println("  list");
    }
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("help")) { usage(); return; }
        try {
            List<Shift> shifts = load();
            if (args[0].equals("list")) {
                if (shifts.isEmpty()) System.out.println("No shifts yet.");
                shifts.stream().sorted(java.util.Comparator.comparing(Shift::date).thenComparing(Shift::start))
                        .forEach(shift -> System.out.println(shift.date() + "  " + shift.start() + "-"
                                + shift.end() + "  " + shift.person()));
                return;
            }
            if (!args[0].equals("add") || args.length != 5) {
                usage();
                throw new IllegalArgumentException("Invalid command or argument count.");
            }
            String person = args[1].trim();
            if (person.isEmpty()) throw new IllegalArgumentException("Person name cannot be blank.");
            LocalDate date = LocalDate.parse(args[2]);
            LocalTime start = LocalTime.parse(args[3]);
            LocalTime end = LocalTime.parse(args[4]);
            if (!end.isAfter(start)) throw new IllegalArgumentException("End time must be after start time.");
            for (Shift shift : shifts) {
                boolean samePerson = shift.person().equalsIgnoreCase(person);
                boolean sameDay = shift.date().equals(date);
                boolean overlap = start.isBefore(shift.end()) && shift.start().isBefore(end);
                if (samePerson && sameDay && overlap) {
                    throw new IllegalArgumentException("This shift overlaps an existing shift for " + person + ".");
                }
            }
            shifts.add(new Shift(person, date, start, end));
            save(shifts);
            System.out.println("Shift added for " + person + " on " + date + ".");
        } catch (Exception error) {
            System.err.println("Error: " + error.getMessage());
            System.exit(1);
        }
    }
}
