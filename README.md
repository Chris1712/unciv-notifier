# Unciv-notifier

Project for monitoring an [unciv](https://github.com/yairm210/unciv) save and notifying the relevant player on their turn.

## How it works

1. `Monitor` watches the unciv save directory, using the path supplied as an argument to the service.
2. When the file is changed (IE a turn is complete), we use `UncivParser` to load the file and determine the next turn taker
3. Notify that person using `Notifier`

## How to build

This project builds with gradle, the included wrapper scripts in the root directory (`gradlew` and `gradlew.bat`)
are self-contained and should require only a recent JVM.

`./gradlew build` populates the `build/distributions` with a tar and a zip file, these have identical contents.

Extract where needed and use `unciv-notifier/bin/unciv-notifier` (or the equivalent bat in windows) to run the service.

## Deployment

`create-release.sh` will build the latest version and tag as a release on github.
Requires github cli to be configured locally.

[Latest version URL](https://github.com/Chris1712/unciv-notifier/releases/latest/download/unciv-notifier.zip)

The 'citadel' repo uses this URL to configure the service running on citadel.

## TODO
- Finish functionality (save file parsing, discord integration)
- Don't double notify
- Carefully double notify, if on the same turn for a long time
- Improve source of config (Save file location, uuid -> discord mappings)
- Better release process