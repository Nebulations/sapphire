<h1 align="center">Sapphire</h1>
<p align="center"><i>An all-in-one moderation suite for Minecraft servers.</i></p>

> [!CAUTION]
> This project is no longer maintained. Feel free to fork the project and customize as you want!

## Features
- **Punishments**: warn, mute, and ban players through a GUI or command, with configurable offense escalation per punishment type. You can also click the names of players in-chat to open their punishment GUI.
- **Reports**: players can report others in-game; staff can view, claim, and act on reports through a dedicated GUI.
- **Chat filter**: automatically catches and punishes messages that match configurable regex patterns.
- **Staff tools**: staff chat, chat clearing, chat muting, vanish, and inventory viewing.
- **Discord integration**: logs punishments, reports, and chat filter triggers to Discord, with support for running staff commands from your server.
- **Flexible storage**: supports both SQLite and MySQL.

## Commands
| Command | Aliases | Description                             |
| --- | --- |-----------------------------------------|
| `/punish <player>` | | Open the punishment GUI for a player.   |
| `/revert <player>` | | Revert a player's punishment..          |
| `/history <player>` | | View the punishment history of a player. |
| `/report <player>` | | Report a player to staff.               |
| `/reports <player>` | | View reports made against a player.     |
| `/editreport <id>` | | Edit the status of a report.            |
| `/claimreport <id>` | | Claim a report.                         |
| `/sapphire` | | Base plugin command.                    |
| `/staffchat <message>` | `/sc`, `/s`, `/schat` | Sends a message to staff chat.          |
| `/clearchat` | `/cc` | Clears in-game chat                     |
| `/mutechat` | `/lockchat` | Mute or unmute the chat.                |
| `/vanish` | `/v` | Hides you from other players.           |
| `/inventorysee <player>` | `/invsee`, `/inv` | Views a player's inventory.             |

## Installation
1. Download the latest build of Sapphire, or build it yourself:
```bash
git clone https://github.com/Nebulations/Sapphire.git
cd Sapphire
mvn clean package
```
2. Drop the resulting jar from `target/` into your server's `plugins` folder.
3. Start (or restart) your server to generate the default configuration files.
4. Edit `plugins/Sapphire/config.yml`, `punishments.yml`, and `chatfilter.yml` to fit your server.
5. Use `/sapphire reload` or restart your server for changes to take effect.

## Requirements
- Java 21
- A Paper (or Paper-fork) server running 1.21+

## Configuration
All settings live in `plugins/Sapphire/`:
- `config.yml` — general settings, message formatting, storage, and Discord integration.
- `punishments.yml` — punishment types, offense escalation, and punishment messages.
- `chatfilter.yml` — chat filter patterns and their associated punishments.

