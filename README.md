# Voting Plugin

A comprehensive poll plugin for Minecraft Paper servers that allows players to create custom polls with persistent storage and modern Adventure API formatting.

![Java](https://img.shields.io/badge/Java-21-orange)
![Paper](https://img.shields.io/badge/Paper-1.21+-green)
![License](https://img.shields.io/badge/License-MIT-blue)

## Features

- 🗳️ **Custom Poll IDs** - Create polls with memorable custom IDs (3-16 characters)
- 💾 **Persistent Storage** - Polls automatically save to JSON and survive server restarts
- 🎨 **Modern Formatting** - Uses Adventure API MiniMessage for beautiful chat messages
- 🔒 **Thread-Safe** - Concurrent data structures for reliable multi-player usage
- 📊 **Real-time Statistics** - View poll results, vote counts, and time remaining
- 🛡️ **Permission-Based** - Secure access control with `voting.poll` permission
- 🔄 **Backward Compatible** - Supports both custom IDs and UUID short IDs for existing polls

## Quick Start

1. **Download** the latest JAR file from the releases page
2. **Install** by placing the JAR in your server's `plugins/` folder
3. **Grant permissions** to users (`voting.poll` permission required)
4. **Restart** your server and start creating polls!

## Commands

| Command | Description | Permission | Example |
|---------|-------------|------------|---------|
| `/poll create <id> "question" "options"` | Create a new poll | `voting.poll` | `/poll create best-food "What's the best food?" "Pizza, Burger, Tacos"` |
| `/poll start <id>` | Start voting on a poll | `voting.poll` | `/poll start best-food` |
| `/poll stop <id>` | Stop voting on a poll | `voting.poll` | `/poll stop best-food` |
| `/poll vote <id> <option>` | Vote in an active poll | `voting.poll` | `/poll vote best-food 2` |
| `/poll unvote <id>` | Remove your vote from a poll | `voting.poll` | `/poll unvote best-food` |
| `/poll stats <id>` | View poll statistics and results | `voting.poll` | `/poll stats best-food` |
| `/poll list` | List all polls you've created | `voting.poll` | `/poll list` |

## Installation

### Requirements

- **Java 21** or higher
- **Paper 1.21** or newer (tested on 1.21.10)
- **Permissions plugin** (optional, for access control)

### Setup Instructions

1. **Download** the latest `Voting-1.0.0.jar` from the [Releases](https://github.com/touchie771/voting/releases) page
2. **Place** the JAR file in your server's `plugins/` directory
3. **Restart** your server or run `/reload`
4. **Grant permissions** to users who should be able to create and manage polls:
   ```
   /lp group default permission set voting.poll true
   ```

### Configuration

No configuration file is required! Polls are automatically stored in:
```
plugins/Voting/polls.json
```

The plugin handles all persistence automatically - polls survive server restarts and crashes.

## Usage Guide

### Creating Polls

Poll IDs must be **3-16 characters** and can contain:
- Lowercase letters (a-z)
- Numbers (0-9)
- Underscores (_) and hyphens (-)

**Auto-lowercasing:** Custom IDs are automatically converted to lowercase. If you type `My-Poll`, it will be stored and used as `my-poll`.

**Reserved IDs** (cannot be used as poll IDs):
```
list, active, create, start, stop, vote, unvote, stats
```
*Note: "active" is reserved for future functionality*

**Example:**
```
/poll create server-update "Should we update to 1.21?" "Yes, No, Maybe"
```

### Voting Process

1. **Create** a poll using `/poll create`
2. **Start** the poll using `/poll start`
3. **Vote** using `/poll vote <id> <option-number>`
4. **View results** using `/poll stats <id>`
5. **Stop** the poll using `/poll stop`

### Poll Duration

All polls run for **5 minutes** (300 seconds) by default. The time remaining is shown in the stats command.

### Poll Management

- **Only poll creators** can start and stop their own polls
- **All players** can vote in active polls created by others
- **Polls automatically expire** after duration ends (marked as inactive, not deleted)

**Example Poll Stats Output:**
```
=== Poll Stats ===
ID: a1b2c3d4              // Short ID (first 8 chars of UUID)
Custom ID: best-food       // Your custom poll identifier
Question: What's the best food?
Creator: PlayerName
Status: Active
Time left: 245 seconds
Options:
1. Pizza (12 votes)
2. Burger (8 votes)
3. Tacos (15 votes)
Total votes: 35
```

## Development

### Building from Source

**Prerequisites:**
- Java 21 JDK
- Git
- Gradle 8.0+

**Build Steps:**
```bash
# Clone the repository
git clone https://github.com/touchie771/voting.git
cd voting

# Build the plugin
./gradlew shadowJar

# Find the built JAR
ls build/libs/Voting-1.0.0.jar
```

### Architecture

```
src/main/java/me/touchie771/voting/
├── Voting.java              # Main plugin class
├── commands/
│   └── PollCommand.java     # All poll commands with Adventure API
└── utils/
    ├── PollManager.java     # Poll management and storage
    └── PollSerializer.java  # JSON persistence with Gson
```

### Dependencies

- **Paper API** 1.21.10 - Server platform
- **LiteCommands** 3.9.2 - Annotation-based command framework
- **Adventure API** - Modern chat formatting
- **Gson** - JSON serialization for persistence

## Limitations

- **No Tab Completion** - LiteCommands 3.9.2 doesn't support custom suggestions (upgrade to newer version for this feature)
- **Fixed Duration** - Poll duration is hardcoded to 5 minutes
- **Single Permission** - All commands use the same `voting.poll` permission

## Troubleshooting

### Common Issues

**Permission Denied Errors:**
```
Ensure users have the 'voting.poll' permission:
/lp user <player> permission set voting.poll true
```

**Polls Not Saving:**
```
Check that the plugins/Voting/ directory is writable:
ls -la plugins/Voting/
```

**Command Not Found:**
```
Verify the plugin is loaded correctly:
/plugins
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

**Enjoy the plugin! 🗳️**