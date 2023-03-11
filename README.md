# Unciv-notifier

Project for monitoring an [unciv](https://github.com/yairm210/unciv) save and notifying the relevant player on their turn.

## How it works

1. `Monitor` watches the unciv save directory, using the path supplied as an argument to the service.
2. When the file is changed (IE a turn is complete), we use `UncivParser` to load the file and determine the next turn taker
3. Notify that person using `Notifier`

## How to deploy

This project builds with gradle, the included wrapper scripts in the root directory (`gradlew` and `gradlew.bat`)
are self-contained and should require only a recent JVM.

`./gradlew build` populates the `build/distributions` with a tar and a zip file, these have identical contents.

Extract where needed and use `unciv-notifier/bin/unciv-notifier` (or the equivalent bat in windows) to run the service.

### Systemd
(TODO)

An example service file is included in root as `unciv-notifier.service` for systemd based systems.
Copy this into /etc/systemd/system/unciv-notifier.service and edit the `ExecStart` line to point to the correct location of the `unciv-notifier` script.