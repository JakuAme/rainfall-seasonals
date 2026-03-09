package com.blockymmc.rainfall.items;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SeasonalItemRegistry {

    private final Map<String, ItemDefinition> items = new HashMap<>();
    private final Logger log;

    public SeasonalItemRegistry(Logger log) {
        this.log = log;
    }

    public void load(File cratesFolder) {
        items.clear();

        if (!cratesFolder.exists()) {
            cratesFolder.mkdirs();
            log.info("Created crates folder: " + cratesFolder.getPath());
        }

        File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            log.warning("No .yml files found in crates folder!");
            return;
        }

        int totalLoaded = 0;
        for (File file : files) {
            totalLoaded += loadFile(file);
        }
        log.info("Total seasonal items loaded: " + totalLoaded);
    }

    private int loadFile(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int count = 0;
        for (String itemId : yaml.getKeys(false)) {
            ConfigurationSection sec = yaml.getConfigurationSection(itemId);
            if (sec == null) continue;

            String type = sec.getString("type", "standard");
            ItemDefinition def = parseDefinition(type, itemId, sec, file.getName());
            if (def == null) continue;

            items.put(itemId, def);
            count++;
        }
        log.info("Loaded " + count + " item(s) from " + file.getName());
        return count;
    }

    private ItemDefinition parseDefinition(String type, String itemId, ConfigurationSection sec, String fileName) {
        switch (type) {
            case "standard": return parseStandard(itemId, sec, fileName);
            default:
                log.warning("Unknown item type '" + type + "' for '" + itemId + "' in " + fileName);
                return null;
        }
    }

    private ItemDefinition parseStandard(String itemId, ConfigurationSection sec, String fileName) {
        String nexoId        = sec.getString("nexo_id");
        String itemName      = sec.getString("name");
        String exclusiveLine = sec.getString("exclusive_line");
        String mysteryEnch   = sec.getString("mystery_enchant");
        int mysteryMin       = sec.getInt("mystery_min", 1);
        int mysteryMax       = sec.getInt("mystery_max", 1);

        if (nexoId == null || itemName == null || exclusiveLine == null || mysteryEnch == null) {
            log.warning("Skipping '" + itemId + "' in " + fileName + ": missing required fields.");
            return null;
        }

        Map<Enchantment, Integer> enchants = new LinkedHashMap<>();
        ConfigurationSection enchSec = sec.getConfigurationSection("enchants");
        if (enchSec != null) {
            for (String key : enchSec.getKeys(false)) {
                Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key));
                if (ench == null) {
                    log.warning("Unknown enchantment '" + key + "' in " + itemId + " (" + fileName + ")");
                    continue;
                }
                enchants.put(ench, enchSec.getInt(key));
            }
        }

        return new StandardItemDefinition(nexoId, itemName, enchants, mysteryEnch, mysteryMin, mysteryMax, exclusiveLine);
    }

    public ItemStack build(String itemId) {
        ItemDefinition def = items.get(itemId);
        return def != null ? def.build() : null;
    }

    public boolean exists(String itemId) {
        return items.containsKey(itemId);
    }
}
