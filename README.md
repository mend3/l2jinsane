# L2Extalia Server - Datapack Interlude 2025

> More info at [datapack-interlude-pvp-2025-insane](https://www.l2jbrasil.com/topic/147731-datapack-interlude-pvp-2025-insane/)

A modern, feature-rich Lineage 2 private server emulator based on the Interlude Chronicle. Built for high performance PvP gameplay with extensive customization options.

**Created by:** mikado  
**Original Release:** April 16, 2024  
**Last Updated:** August 31, 2025

## Project Overview

```
l2jserver/
├── src/                    # Java source code
│   ├── net/sf/l2j/        # Core server packages
│   │   ├── gameserver/    # Game server implementation
│   │   ├── loginserver/   # Login server implementation
│   │   ├── commons/       # Shared utilities
│   │   └── util/          # Utility classes
│   ├── mods/              # Custom gameplay modifications
│   └── enginemods/        # Engine-level modifications
├── config/                 # Server configuration
│   ├── engine/            # Engine mod settings (17 files)
│   └── events/            # Event configurations (6 files)
├── data/                   # Game data files
│   ├── xml/               # XML definitions (skills, items, NPCs)
│   ├── html/              # NPC dialogs and interfaces
│   ├── geodata/           # Pathfinding data
│   └── crests/            # Clan crests storage
├── sql/                    # Database schema files
├── libs/                   # External dependencies
├── tools/                  # Server management scripts
└── log/                    # Server logs
```

## Key Features

### Core Systems
- **Modern Java 21 codebase** with modular, interface-based design
- **HikariCP connection pooling** for database performance
- **MMOCore networking** for low-latency, high-throughput gameplay
- **GeoEngine** with pathfinding and line-of-sight calculations
- **Community Board** with customizable HTML interface
- **HWID Protection** for anti-multibox and security

### Events System

| Event | Description |
|-------|-------------|
| **TvT** | Team vs Team battles |
| **CTF** | Capture The Flag |
| **DM** | Death Match |
| **Kill The Boss (KTB)** | Boss hunting competition |
| **PvP Event** | PvP competition with rewards |
| **Tournament** | Competitive arena (2v2, 4v4, 9v9) |
| **Solo Boss Event** | Cooperative - everyone in death radius receives rewards |
| **PC Bang Event** | Internet cafe reward system |

### Special Zones

| Zone | Description |
|------|-------------|
| **Farm Zone** | AutoFarm enabled, grants Sweeper & Spoil skills on entry (HWID optional) |
| **Party Zone** | Group farming area (HWID optional) |
| **Random PvP Zone** | Rotating map system with rewards |
| **MultifunctionZone** | Configurable special zone with custom rules |

### Special Rewards
- **Login Reward** - Every 24 hours
- **Daily Reward** - 1 reward per day (HWID tracked, lost if unclaimed)
- **Level Rewards** - Configurable rewards at specific levels (70-80)
- **Reward Boxes** - Loot box system with random rewards

### Datapack Specials

| Feature | Description |
|---------|-------------|
| **Agathions** | Pet companions with optional buffs |
| **Combine Item** | Item combination/fusion system |
| **Dolls System** | Collectible dolls with bonuses |
| **DressMe System** | Cosmetic armor & weapon skins |
| **Dungeons** | Solo & party instance dungeons |

### Interface Features
- **Assassin Interface** - Quick access combat tools
- **Buff Cancellation** - Easy buff management
- **AutoShots** - Automatic soulshot/spiritshot
- **Teleport Interface** - Enhanced teleportation UI

### Custom Modifications (mods/)

| Mod | Description |
|-----|-------------|
| Achievement System | Player achievement tracking |
| AutoFarm | Automated farming with zone restrictions |
| Balancer | Skill & class balancing system |
| CombineItem | Item combination system |
| DressMe | Cosmetic skin/appearance system |
| Dungeon | Instance dungeon system |
| Newbies | New player experience enhancements |
| PvP Zone | Custom PvP zone management |
| Teleport Interface | Enhanced teleportation UI |

### Engine Modifications (config/engine/)

| Feature | Description |
|---------|-------------|
| AIO Buffer | All-in-one buffer with custom skills |
| VIP System | Premium membership with stat bonuses |
| Champions | Random champion mob spawns (Weak/Super/Hard) |
| Fake Players | Bot simulation for population |
| Offline Trade | AFK shop functionality |
| PvP Rewards | Configurable kill rewards |
| Spree Kills | Kill streak announcements |
| Boss Events | Random boss spawn events |

## Player Commands

### Event Commands
| Command | Description |
|---------|-------------|
| `.pvpevent` / `.pvpEvent` | Shows ranking during PvP Event |
| `.bossevent` | Register/unregister from Kill Boss Event |
| `.register` | Register for TvT/CTF/DM events |
| `.leave` | Unregister from TvT/CTF/DM events |

### Other Commands
| Command | Description |
|---------|-------------|
| `.sellbuff` | Start selling buffs |
| `.cancelsellbuff` | Stop selling buffs |
| `.dressme` | Open DressMe cosmetic interface |

## Build & Run

### Prerequisites

- **Java JDK 21+** (BellSoft Liberica JDK recommended)
- **MariaDB** (via XAMPP or standalone)
- **make** (for build automation)

### Database Setup

1. Create the database:
```sql
CREATE DATABASE insanepvp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Run the installation script:
```sh
cd tools
./database_installer.sh  # Linux/Mac
database_installer.bat   # Windows
```

Or manually import SQL files from the `sql/` directory.

### Build

```sh
make build
```

This generates build metadata in `config/build.properties` including:
- Build name, version, and timestamp
- Git commit hash
- SHA256 checksum of source files

### Configuration

1. Edit `config/server.properties`:
   - Set `Hostname` to your server IP
   - Configure database URL, Login, and Password
   - Adjust rates and gameplay settings

2. Edit `config/loginserver.properties` for login server settings

3. Customize gameplay in `config/custom.properties`:
   - Skill duration modifications
   - PvP restrictions and rewards
   - Newbie starter kits

### Run

**Using Makefile (recommended):**
```sh
# Start Login Server (in one terminal)
make run-login

# Start Game Server (in another terminal)
make run-game

# Or with custom JDK path
make JAVA_HOME=/path/to/jdk21 run-login
make JAVA_HOME=/path/to/jdk21 run-game
```

**Using provided scripts:**
```sh
cd tools
./startLoginServer.sh   # Start login server
# In another terminal:
java -cp "$CLASSPATH:./libs/*" net.sf.l2j.gameserver.GameServer
```

**Register a game server:**
```sh
cd tools
./RegisterGameServer.sh
```

**Available Makefile commands:**
```sh
make run-login      # Start Login Server
make run-game       # Start Game Server
make run-login-bg   # Start Login Server in background (Linux/WSL)
make run-game-bg    # Start Game Server in background (Linux/WSL)
make build          # Generate build metadata
make debug          # Show current configuration
make check-java     # Verify Java installation
make help           # Show all commands
```

## Server Configuration Reference

### Rate Settings (server.properties)
```properties
RateXp = 1.0              # Experience multiplier
RateSp = 1.0              # Skill point multiplier
RateDropAdena = 1.0       # Adena drop multiplier
RateDropItems = 1.0       # Item drop multiplier
```

### VIP Bonuses (config/engine/Vip.properties)
- 20% stat bonuses (P.Atk, M.Atk, P.Def, M.Def)
- 30% EXP/SP bonus
- 2x event reward multiplier

### Champion System (config/engine/Champions.properties)
- **Weak** (10% spawn): 1.5x stats, 20% bonus drops
- **Super** (5% spawn): 2.0x stats, 20% bonus drops
- **Hard** (1% spawn): 3-4x stats, 20% bonus drops

### Key Configuration Files

| File | Purpose |
|------|---------|
| `config/server.properties` | Main server settings |
| `config/custom.properties` | Custom features & PvP settings |
| `config/events.properties` | Olympiad & Seven Signs |
| `data/xml/enchants.xml` | Enchant rates configuration |
| `data/xml/fake_pcs.xml` | Fake player configuration |
| `data/xml/soloboss.xml` | Solo Boss Event configuration |

## Tools

| Script | Purpose |
|--------|---------|
| `database_installer.sh/bat` | Install database schema |
| `startLoginServer.sh/bat` | Launch login server |
| `RegisterGameServer.sh/bat` | Register game server with login |
| `startSQLAccountManager.sh/bat` | Account management utility |

## Credits

This datapack is a compilation of work from the L2J community, including contributions from:
- L2JBrasil community
- L2DevSAdmins (EngineMods by Fissban)
- Various open-source L2J projects

Special thanks to **mikado** for compiling and maintaining this PvP-focused datapack.

## License

This project is licensed under the **GNU GPL v2** or later. See [LICENSE](LICENSE) for details.

---

**Server Name:** L2Extalia  
**Version:** 1.0.0  
**Chronicle:** Interlude (C6)  
**Style:** PvP-focused gameplay
