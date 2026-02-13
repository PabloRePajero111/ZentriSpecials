# ZentriSpecials

A comprehensive Minecraft Paper plugin with **100% Folia compatibility** that adds multiple custom features for server management and gameplay enhancement.

## Features

### 🆕 NEW Feature 1: Clumps (XP Orb Merging)
Automatically merges nearby XP orbs to reduce lag and improve performance.

- **Folia-Compatible**: Uses entity and global region schedulers
- Configurable merge radius and check intervals
- Maximum XP per orb limit
- World-specific configuration
- Periodic background task for efficient merging

**Configuration:**
```yaml
clumps:
  enabled: true
  merge-radius: 5.0          # radius in blocks to merge orbs
  check-interval: 40          # ticks between merge checks
  max-xp-per-orb: 10000      # maximum XP in a single merged orb
  worlds:                     # worlds where this applies (empty = all)
    - world
    - world_nether
```

### 🆕 NEW Feature 2: Compact Death Drops
Prevents items from scattering when a player dies, keeping them neatly stacked at the death location.

- **Folia-Compatible**: Uses region scheduler for item spawning
- Configurable spread radius (0 = perfectly stacked)
- Adjustable pickup delay for protection
- Respects keepInventory gamerule
- Minimal item velocity for compact drops

**Configuration:**
```yaml
compact-death-drops:
  enabled: true
  spread-radius: 0.3         # maximum scatter radius (blocks), 0 = same point
  pickup-delay: 20            # ticks before items can be picked up (0 = immediate)
  y-velocity: 0.1             # vertical velocity of dropped items
  worlds:                     # worlds where this applies (empty = all)
    - world
    - world_nether
```

### Item Cooldown by Region (WorldGuard)
Set material-specific cooldowns in WorldGuard regions.

- Per-region, per-material cooldown configuration
- Smooth integration with WorldGuard
- Customizable messages
- Visual feedback to players

### Max Uses by Region (WorldGuard)
Limit the number of times specific items can be used in WorldGuard regions.

- Configurable max uses per item per region
- Auto-reset on region leave (optional)
- Time-based reset intervals
- Does not remove items from inventory

### Barrier Entity Kill
Automatically removes entities standing on barrier blocks.

- **Folia-Compatible**: Uses global and entity schedulers
- Configurable entity type filters
- Region-specific application
- Adjustable check intervals

### Fly in Spawn & ProtectionStones
Automatic flight management in spawn and owned ProtectionStones regions.

- Auto-enable fly in configured spawn region (with permission)
- Auto-enable fly in owned ProtectionStones regions
- Anti-fall damage protection when fly is disabled
- Permission-based access control

### Disabled Items by Region (WorldGuard)
Completely disable specific items in WorldGuard regions.

- Total block of item usage (all interaction events)
- Items remain in inventory
- Customizable messages per region
- Support for both items and blocks

### Fix Commands
Item repair commands with permission-based cooldowns.

- `/fixhand` - Repair item in hand
- `/fixall` - Repair all items in inventory
- Rank-based cooldowns (default, VIP, MVP)
- Customizable messages

## Technical Details

### Folia Compatibility
This plugin is **100% Folia-compatible**. It NEVER uses `Bukkit.getScheduler()` and instead uses:

- `entity.getScheduler()` - For entity-specific tasks (e.g., XP orb merging)
- `Bukkit.getRegionScheduler()` - For location-specific tasks (e.g., death drops)
- `Bukkit.getGlobalRegionScheduler()` - For global periodic tasks (e.g., barrier entity kill)
- `player.getScheduler()` - For player-specific tasks (e.g., fly checks)

### Requirements

- **Java**: 21
- **Server**: Paper 1.21+
- **Dependencies**:
  - WorldGuard 7.x (required)
  - ProtectionStones API (optional, for PS fly feature)

### Build

Built with **Gradle Kotlin DSL**:

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/ZentriSpecials-1.0.0.jar`

## Commands

- `/fixhand` - Repair the item in your hand
  - Permission: `zentrispecials.fixhand`
- `/fixall` - Repair all items in your inventory  
  - Permission: `zentrispecials.fixall`
- `/zentrispecials reload` (alias: `/zs reload`) - Reload the plugin configuration
  - Permission: `zentrispecials.reload`

## Permissions

- `zentrispecials.admin` - Access to admin commands (default: op)
- `zentrispecials.reload` - Reload the plugin configuration (default: op)
- `zentrispecials.fixhand` - Use the fixhand command (default: true)
- `zentrispecials.fixhand.vip` - Reduced cooldown for fixhand
- `zentrispecials.fixhand.mvp` - Minimal cooldown for fixhand
- `zentrispecials.fixall` - Use the fixall command (default: op)
- `zentrispecials.fixall.vip` - Reduced cooldown for fixall
- `zentrispecials.fixall.mvp` - Minimal cooldown for fixall
- `zentrispecials.fly.spawn` - Allow fly in spawn region (default: op)
- `zentrispecials.fly.protectionstones` - Allow fly in own PS regions (default: true)

## Configuration

Full configuration available in `config.yml`. All features can be enabled/disabled and customized independently.

See the default config for all available options and examples.

## Installation

1. Download the latest release JAR
2. Place in your server's `plugins/` folder
3. Ensure WorldGuard is installed (required dependency)
4. Optionally install ProtectionStones for PS fly feature
5. Start/restart your server
6. Configure the plugin in `plugins/ZentriSpecials/config.yml`
7. Reload with `/zs reload`

## Support

For issues, questions, or feature requests, please open an issue on the GitHub repository.

## License

This plugin is developed for the Zentri server.