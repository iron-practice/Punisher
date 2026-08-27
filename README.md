# IronPunisher

IronPunisher is a Paper punishment plugin designed for networks where every server shares one MySQL/MariaDB database.

## Commands

```text
/ban <player> <reason> <duration>
/punish <player> <reason> <duration>
/unpunish <player>
/unban <player>
```

### Durations

```text
1d      1 day
3d      3 days
7d      7 days
2w      2 weeks
1mo     1 month
6mo     6 months
perma   permanent
```

German/long aliases such as `1tag`, `3tage`, `2wochen`, `1monat` and `6monate` are also accepted.

## Network setup

1. Create one MySQL/MariaDB database.
2. Put the same database credentials into `config.yml` on every Paper server.
3. Give every server a different `server-id`.
4. Build the shaded jar with Gradle and install the same jar on every server.

A ban created on one server is stored centrally. The login listener checks the UUID against that database before the player is admitted. A small asynchronous synchronization task also catches players who are already online on another server.

## Important

The plugin does not depend on Vulcan or another anticheat. Punishments are created through the plugin command and stored centrally.
