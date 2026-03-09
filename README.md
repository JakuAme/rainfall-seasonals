# Rainfall

A Paper Minecraft plugin for BlockyMC that handles seasonal crate item generation. It builds custom-enchanted Nexo items with Adventure gradient names, AdvancedEnchantments mystery enchants, and styled lore — all driven by YAML config files with no recompile needed.

---

## How It Works

When a player wins a gear reward from a crate (e.g. the Valentine Crate), ExcellentCrates runs a command reward:

```
/rainfall <player> <itemId>
```

Rainfall intercepts this, looks up the item definition from its YAML config, and builds the item from scratch:

1. **Nexo base item** — fetches the custom-textured item via `NexoItems.itemFromId()`
2. **Vanilla enchants** — applies all enchants at their correct levels via `addUnsafeEnchantment`
3. **Gradient item name** — sets a MiniMessage gradient name (non-italic) via Adventure API
4. **Hides vanilla enchant lore** — uses `ItemFlag.HIDE_ENCHANTS` so Minecraft doesn't auto-generate enchant tooltips
5. **Mystery enchant** — applies a weighted-random-level AdvancedEnchantments enchant via `AEAPI.applyEnchant()`. Level is drawn using hardcoded weighted tables (supporting max tier 5 and 10) — lower tiers are more common, max tier is rare
6. **AE lore organization** — calls `AEAPI.organizeEnchants()` to sort AE lore into its groups
7. **Max tier bold** — if the rolled level equals `mystery_max`, all AE enchant lore lines are bolded to visually distinguish a max tier roll
8. **Vanilla enchant lore** — manually appends styled gray `✦ Enchant Level` lines in YAML definition order
9. **Exclusive lore line** — appends a blank line + a gradient "Crate Exclusive" footer at the bottom

---

## Adding a New Crate

No recompile needed. Just:

1. Create a new `.yml` file in `plugins/Rainfall/crates/` (e.g. `stpatricks.yml`)
2. Define your items (see format below)
3. Restart the server

All `.yml` files in the `crates/` folder are loaded automatically on startup.

---

## YAML Item Format

```yaml
item_id:
  nexo_id: your_nexo_item_id
  name: "<gradient:#COLOR1:#COLOR2:#COLOR1>Item Display Name</gradient>"
  exclusive_line: "<gradient:#COLOR1:#COLOR2:#COLOR1>★ Crate Name Exclusive ★</gradient>"
  mystery_enchant: ae_enchant_internal_name
  mystery_min: 1
  mystery_max: 10
  enchants:
    protection: 7
    unbreaking: 10
    mending: 1
```

| Field | Description |
|-------|-------------|
| `type` | Item definition type (`standard` by default) |
| `nexo_id` | The Nexo item ID (from your Nexo items `.yml`) |
| `name` | MiniMessage string for the item's display name |
| `exclusive_line` | MiniMessage string shown at the bottom of the lore |
| `mystery_enchant` | Internal name of the AE enchant to apply randomly |
| `mystery_min` | Minimum tier for the mystery enchant (inclusive) |
| `mystery_max` | Maximum tier for the mystery enchant (inclusive) |
| `enchants` | Map of vanilla enchant keys → levels |

Vanilla enchant keys use Minecraft's internal names (e.g. `fire_protection`, `sweeping_edge`, `luck_of_the_sea`).

---

## ExcellentCrates Integration

In your crate `.yml` (e.g. `plugins/ExcellentCrates/crates/valentine.yml`), gear rewards should be set up as command rewards:

```yaml
your_reward_key:
  Items:
    Main:
      Type: COMMAND
      Commands:
        - rainfall %player_name% your_item_id
```

---

## Command

| Command | Permission | Description |
|---------|-----------|-------------|
| `/rainfall <player> <itemId>` | `rainfall.give` | Gives the specified seasonal item to a player |

---

## Dependencies

| Plugin | Version | Notes |
|--------|---------|-------|
| [Nexo](https://nexo.io) | 1.16.1+ | Provides the base custom-textured items |
| [AdvancedEnchantments](https://advancedenchantments.net) | 9.22.5+ | Applies and organizes mystery enchants |

---

## Project Structure

```
src/main/java/com/blockymmc/rainfall/
├── RainfallPlugin.java              # Plugin entry point, holds registry instance
├── GiveCommand.java             # /rainfall command handler
└── items/
    ├── ItemDefinition.java          # Interface: all item types implement build()
    ├── StandardItemDefinition.java  # Standard seasonal item (Nexo + vanilla enchants + AE mystery enchant)
    └── SeasonalItemRegistry.java    # Loads YAMLs, dispatches by type, holds item map

src/main/resources/
├── plugin.yml
└── crates/
    └── valentine.yml            # Default Valentine crate items (copied on first run)
```

---

## Build

Requires Java 21 and Maven.

```bash
mvn package -Dmaven.compiler.fork=true
```

Output: `target/rainfall-1.0.0.jar`
