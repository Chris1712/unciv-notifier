# Unciv-notifier

Project for monitoring an [unciv](https://github.com/yairm210/unciv) save and notifying the relevant player on their turn.

## How it works

1. `Monitor` watches the unciv save preview file
(EG `/var/unciv-saves/21f819a1-ed62-432d-ac57-9892af3e7c00_Preview`,
using the path supplied as an argument to the service.
2. When the file is changed (IE a turn is complete), we use `UncivParser` to load the file and determine the next turn taker
3. Notify that person using `Notifier`

## How to use

### Building

This project builds with gradle, the included wrapper scripts in the root directory (`gradlew` and `gradlew.bat`)
are self-contained and should require only a recent JVM.

`./gradlew build` populates the `build/distributions` with a tar and a zip file, these have identical contents.

### Deploying

Extract where needed and use `unciv-notifier/bin/unciv-notifier` (or the equivalent bat in windows) to run the service.

`create-release.sh` will build the latest version and tag as a release on github.
Requires github cli to be configured locally.

[Latest version URL](https://github.com/Chris1712/unciv-notifier/releases/latest/download/unciv-notifier.zip)

The 'citadel' repo uses this URL to configure the service running on citadel.

### Running

The project loads config (save file location, discord bot token, etc.) from a file called `config.yaml`,
this file's path should be provided as the sole argument to the service when executed. A sample config file is included
as `sample-config.yaml` in src/test/resources.

## Discord integration

The service uses a discord bot to notify players.
See [discord docs for more info](https://discord.com/developers/docs/topics/oauth2#bots)

To add to a server you control click this link: (URL is bot's application id and permissions into, 2048 -> send messages

https://discord.com/oauth2/authorize?client_id=1084232402496389193&scope=bot&permissions=2048

## TODO
- Finish functionality (save file parsing, discord integration)
- Don't double notify
- Carefully double notify, if on the same turn for a long time
- Improve source of config (Save file location, uuid -> discord mappings)
- Better release process