# Shift Lite

A small Java command-line scheduler for keeping a team's shifts in a local file and catching overlaps before they are saved.

## Features

- Add and list shifts.
- Rejects an end time that is not after the start time.
- Warns when the same person already has an overlapping shift on the same date.
- Stores data in shifts.tsv; no network connection is used.

## Requirements

JDK 17 or newer. No third-party packages.

## Build and run

~~~sh
javac ShiftLite.java
java ShiftLite add "Sam Lee" 2026-10-01 09:00 12:00
java ShiftLite list
~~~

Use 24-hour local times. The data file is created in the current directory.
