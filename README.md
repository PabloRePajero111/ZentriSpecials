# ZentriSpecials

**Advanced special features plugin for Minecraft Paper/Folia 1.21+**

## Features

### 1. Item Cooldowns by Region (WorldGuard)
Apply vanilla item cooldowns to players when they use items within specific WorldGuard regions. Fully configurable per region and per material.

### 2. Max Uses of Items by Region (WorldGuard)
Limit the number of times a player can use an item within a WorldGuard region. Supports automatic reset when leaving the region or after a configurable time period.

### 3. Barrier Entity Kill
Automatically removes entities (excluding players) standing on barrier blocks within configured WorldGuard regions. Uses Folia-compatible region schedulers.

### 4. Fly in Spawn & ProtectionStones
Enable flight for players in two scenarios:
- In the configured WorldGuard spawn region (requires permission)
- In ProtectionStones regions where the player is an owner or member (requires permission)

Includes safe landing mechanics with slow falling effect.

### 5. Disabled Items in Regions
Completely block the use of specific items in WorldGuard regions. Prevents:
- Item interactions (right/left click)
- Block placement
- Item consumption
- Bucket usage
- Bow/crossbow shooting
- Projectile launches
- And more

### 6. Fix Commands
Repair items with permission-based cooldowns:
- `/fixhand` - Repair item in main hand
- `/fixall` - Repair all items in inventory

Cooldown system supports different times based on player permissions (staff, MVP, VIP, default).

## Requirements

- **Minecraft:** Paper or Folia 1.21+
- **Java:** 21+
- **Dependencies:**
  - WorldGuard 7.x
  - ProtectionStones API

## Folia Compatibility

This plugin is **100% Folia-compatible**:
- ✅ Uses `entity.getScheduler()` for entity tasks
- ✅ Uses `Bukkit.getRegionScheduler()` for region-based tasks
- ✅ Uses `Bukkit.getAsyncScheduler()` for async tasks
- ❌ Never uses `Bukkit.getScheduler()`

## Installation

1. Download the latest release
2. Place the JAR file in your server's `plugins/` folder
3. Install dependencies: WorldGuard and ProtectionStones
4. Restart the server
5. Configure the plugin in `plugins/ZentriSpecials/config.yml`
6. Reload with `/zentrispecials reload`

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/fixhand` | `zentrispecials.fix` | Repair item in hand |
| `/fixall` | `zentrispecials.fix` | Repair all items in inventory |
| `/zentrispecials reload` | `zentrispecials.reload` | Reload configuration |

## Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `zentrispecials.fix` | false | Access to fix commands |
| `zentrispecials.fix.staff` | op | Staff fix cooldown (0s) |
| `zentrispecials.fix.mvp` | false | MVP fix cooldown (30s) |
| `zentrispecials.fix.vip` | false | VIP fix cooldown (60s) |
| `zentrispecials.fix.default` | true | Default fix cooldown (120s) |
| `zentrispecials.fly` | false | Fly in spawn/owned protections |
| `zentrispecials.reload` | op | Reload plugin configuration |

## Configuration

The plugin is highly configurable. See `config.yml` for all options. Each feature can be enabled/disabled independently.

Example configuration snippets:

### Item Cooldowns
```yaml
item-cooldowns:
  enabled: true
  regions:
    arena:
      ENDER_PEARL: 200  # 10 seconds in ticks
      GOLDEN_APPLE: 100
```

### Max Uses
```yaml
item-max-uses:
  enabled: true
  regions:
    arena:
      ENDER_PEARL:
        max-uses: 3
        reset-on-leave: true
        reset-time: 300  # seconds
```

### Disabled Items
```yaml
disabled-items:
  enabled: true
  regions:
    spawn:
      - ENDER_PEARL
      - TNT
      - LAVA_BUCKET
```

## Building

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## Support

For issues, feature requests, or questions, please open an issue on GitHub.

## License

This project is licensed under the MIT License.