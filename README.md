# Unciv-notifier

Project for monitoring an [unciv](https://github.com/yairm210/unciv) save and notifying the relevant player on their turn.

## How it works

1. The unciv save directory is monitored (using the configured path)
2. When the file is changed (IE a turn is complete), determine the next turn taker

## How to deploy

This project builds with gradle, the included wrapper scripts in the root directory (`gradlew` and `gradlew.bat`)
are self-contained and should require only a recent JVM.

`./gradlew build` populates the `app/build/distributions` with a tar and a zip file, these have identical contents.

Extract where needed and use `app/bin/app` (or `app/bin/app.bat` in windows) to run the service. 