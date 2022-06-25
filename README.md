# DisableReports

Minecraft plugin that disables the new reports in 1.19.1 by removing any traces of Microsoft accounts from the messages.  
It is highly customizable, you can specify to only disable reports for some players, for example.

## URLs

- Spigot (coming soon)
- bStats (coming soon)

## Config

The default `config.yml` looks like this:
```yaml
# Whether to check for updates on enabling.
checkForUpdates: true

# Used to specify the players for whom reports should be disabled.
players:
  # How to choose the players to disable reports for. Possible values:
  # - "ALL": Disable reports for all players.
  # - "OPERATORS": Only disable reports for operators.
  # - "PERMISSION": Only disable reports for players with the permission "disablereports.permission"
  # - "SPECIFIC": Only disable reports for the specified players.
  type: ALL

  # Only used when "type" is "SPECIFIC". The players to disable reports for.
  # You can state UUIDs or player names. UUIDs are recommended.
  specificPlayers: []

# Used to turn off disabling reports for specific players.
turnOff:
  # Whether or not to enable the command "/disablereports off" that players can use to turn off disabling reports for themselves.
  # The permission for that command is "disablereports.off" and "disablereports.off.others". If you want all players to be able to use the command, you can use a permission plugin like LuckPerms.
  # If you enable this option and the warning message is enabled, it is recommended to include the command in the warning message.
  # Warning: when having this option enabled the plugin will edit the config and you will have to pay attention to that when editing the config yourself.
  allowChangePerCommand: false

  # The players for whom disabling reports should be turned off.
  # You can state UUIDs or player names. UUIDs are recommended.
  players: []

# Used to control whether or not a warning is sent to players on join.
# This option exists because when players have "secure chat" enabled, it may cause problems when using this plugin.
# You can edit the warning message in the messages.yml file.
warning:
  # Whether or not the message is sent.
  enabled: false

  # Whether or not the message should only be sent to players for whom reports are actually disabled (see "players" and "turnOff").
  # If disabled, the message will be sent to all players.
  onlyForSpecifiedPlayers: true
```

### Messages

You can change all messages in the `messages.yml` file.

## Commands

The plugin has one basic command: `/disablereports`.
The alias is `/dr`.
Sub commands are:
- `/disablereports reload` - Reloads the config and messages.
- `/disablereports off [Player]` - Toggles turning off disabling reports for yourself or other players.  
  This command is only available if you have set `turnOff.allowChangePerCommand` to `true`.

## Permissions

- `disablereports.reload` - Permission for `/disablereports reload`.
- `disablereports.off` - Permission for `/disablereports off`.
- `disablereports.off.others` - Permission for `/disablereports off <Player>`.
- `disablereports.permission` - Permission that is used to specify for which players the reports should be disabled when `players.type` is set to `PERMISSION`.

## Additional information

This plugin collects anonymous server stats with [bStats](https://bstats.org), an open-source statistics service for Minecraft software. If you don't want this, you can deactivate it in `plugins/bStats/config.yml`.
