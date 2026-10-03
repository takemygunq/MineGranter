# MineGranter

A Spigot / Paper plugin for managing a Minecraft server's staff team: staff roles, on-shift / off-shift mode
for helpers and moderators, and player reports — with notifications to Telegram.

## Features

- **Staff roles in one command.** `/staff give <player> <role>` assigns the role through LuckPerms
  and announces it in chat; `/staff take <player>` removes it.
- **Shifts.** Off shift, a staff member stays in an "off" group with no staff permissions.
  `/sw start` switches them to the working group and runs configurable commands (god mode and fly by default);
  `/sw stop` switches back and, by default, teleports them to spawn.
- **On-shift restrictions.** Staff on shift can't attack players, pick up items or open containers,
  and their shift ends automatically when they leave the server. Every restriction can be turned off.
- **Player reports.** `/report <player> <reason>` with a cooldown, nickname validation and report immunity.
- **Telegram notifications** for new and removed staff, shifts and reports — sent asynchronously,
  so the server never waits for Telegram.
- **Everything is configurable**: roles, LuckPerms commands, shift commands and every message (`&` color codes).
- Tab completion for all commands, `/staff reload` without restarting the server.

## Requirements

- Spigot or Paper **1.20+**, Java **17+**
- [LuckPerms](https://luckperms.net/) — groups and permissions
- [EssentialsX](https://essentialsx.net/) — only for the default shift commands (`god`, `fly`, `spawn`);
  replace them in the config if you use something else

## Commands and permissions

| Command | Description | Permission | Default |
|---|---|---|---|
| `/staff give <player> <role>` | make a player a staff member | `minegranter.manage` | op |
| `/staff take <player>` | remove a staff role | `minegranter.manage` | op |
| `/staff reload` | reload `config.yml` | `minegranter.manage` | op |
| `/sw start` / `/sw stop` | start or end a shift | `minegranter.shift` | — |
| `/sw check <player>` | show a player's role and shift status | `minegranter.shift.check` | op |
| `/report <player> <reason>` | report a player | `minegranter.report` | everyone |

| Permission | Effect | Default |
|---|---|---|
| `minegranter.report.immune` | can't be reported | op |
| `minegranter.bypass` | ignores on-shift restrictions | op |

Give `minegranter.shift` to your staff groups in LuckPerms (both the working and the "off" groups).
Operators can't use shifts: they implicitly have every `group.*` permission, so roles are detected for non-op players only.

## How roles work

Every role has two LuckPerms groups — create both and give the working one your staff permissions:

```yaml
roles:
  moder:
    prefix: "MODERATOR"     # shown in chat and notifications
    on_group: "moder"       # on shift: staff permissions
    off_group: "offmoder"   # off shift: no staff permissions
```

`/staff give Steve moder` puts Steve into `offmoder`. `/sw start` moves him to `moder`, `/sw stop` back to `offmoder`.
The default config ships with helper, senior helper, junior moderator, moderator and senior moderator.

## Telegram setup

1. Create a bot with [@BotFather](https://t.me/BotFather) and copy its token.
2. Add the bot to your staff chat (and the reports chat, if it's a different one).
3. Get the chat ID — for example, send a message in the chat and open
   `https://api.telegram.org/bot<token>/getUpdates`; group IDs look like `-1001234567890`.
4. Fill in the config and run `/staff reload`:

```yaml
telegram:
  enabled: true
  bot_token: "123456:ABC..."
  staff_chat_id: "-1001234567890"
  reports_chat_id: "-1001234567890"
```

Notification texts are in `telegram.messages`. Keep the bot token private — don't commit your server's config.

## Building

```bash
mvn package
```

The plugin jar ends up in `target/`. Every push is also built by GitHub Actions — the jar is attached to the workflow run.
