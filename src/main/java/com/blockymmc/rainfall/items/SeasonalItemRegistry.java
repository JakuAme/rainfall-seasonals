package com.blockymmc.rainfall.items;

import com.blockymmc.rainfall.RainfallPlugin;
import net.advancedplugins.ae.api.AEAPI;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class SeasonalItemRegistry {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private static final Map<Enchantment, String> ENCHANT_DISPLAY = new LinkedHashMap<>();
    static {
        ENCHANT_DISPLAY.put(Enchantment.PROTECTION,            "Protection");
        ENCHANT_DISPLAY.put(Enchantment.FIRE_PROTECTION,       "Fire Protection");
        ENCHANT_DISPLAY.put(Enchantment.BLAST_PROTECTION,      "Blast Protection");
        ENCHANT_DISPLAY.put(Enchantment.PROJECTILE_PROTECTION, "Projectile Protection");
        ENCHANT_DISPLAY.put(Enchantment.FEATHER_FALLING,       "Feather Falling");
        ENCHANT_DISPLAY.put(Enchantment.AQUA_AFFINITY,         "Aqua Affinity");
        ENCHANT_DISPLAY.put(Enchantment.DEPTH_STRIDER,         "Depth Strider");
        ENCHANT_DISPLAY.put(Enchantment.SHARPNESS,             "Sharpness");
        ENCHANT_DISPLAY.put(Enchantment.SMITE,                 "Smite");
        ENCHANT_DISPLAY.put(Enchantment.SWEEPING_EDGE,         "Sweeping Edge");
        ENCHANT_DISPLAY.put(Enchantment.FIRE_ASPECT,           "Fire Aspect");
        ENCHANT_DISPLAY.put(Enchantment.KNOCKBACK,             "Knockback");
        ENCHANT_DISPLAY.put(Enchantment.EFFICIENCY,            "Efficiency");
        ENCHANT_DISPLAY.put(Enchantment.FORTUNE,               "Fortune");
        ENCHANT_DISPLAY.put(Enchantment.POWER,                 "Power");
        ENCHANT_DISPLAY.put(Enchantment.FLAME,                 "Flame");
        ENCHANT_DISPLAY.put(Enchantment.MULTISHOT,             "Multishot");
        ENCHANT_DISPLAY.put(Enchantment.QUICK_CHARGE,          "Quick Charge");
        ENCHANT_DISPLAY.put(Enchantment.LUCK_OF_THE_SEA,       "Luck of the Sea");
        ENCHANT_DISPLAY.put(Enchantment.LURE,                  "Lure");
        ENCHANT_DISPLAY.put(Enchantment.UNBREAKING,            "Unbreaking");
        ENCHANT_DISPLAY.put(Enchantment.MENDING,               "Mending");
    }

    private static final Map<String, ItemDefinition> ITEMS = new HashMap<>();

    public static void loadFromFolder(File cratesFolder) {
        ITEMS.clear();
        Logger log = RainfallPlugin.getInstance().getLogger();

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
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            int count = 0;
            for (String itemId : yaml.getKeys(false)) {
                ConfigurationSection sec = yaml.getConfigurationSection(itemId);
                if (sec == null) continue;

                String nexoId        = sec.getString("nexo_id");
                String itemName      = sec.getString("name");
                String exclusiveLine = sec.getString("exclusive_line");
                String mysteryEnch   = sec.getString("mystery_enchant");
                int mysteryMin       = sec.getInt("mystery_min", 1);
                int mysteryMax       = sec.getInt("mystery_max", 1);

                if (nexoId == null || itemName == null || exclusiveLine == null || mysteryEnch == null) {
                    log.warning("Skipping '" + itemId + "' in " + file.getName() + ": missing required fields.");
                    continue;
                }

                Map<Enchantment, Integer> enchants = new HashMap<>();
                ConfigurationSection enchSec = sec.getConfigurationSection("enchants");
                if (enchSec != null) {
                    for (String key : enchSec.getKeys(false)) {
                        Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key));
                        if (ench == null) {
                            log.warning("Unknown enchantment '" + key + "' in " + itemId + " (" + file.getName() + ")");
                            continue;
                        }
                        enchants.put(ench, enchSec.getInt(key));
                    }
                }

                ITEMS.put(itemId, new ItemDefinition(nexoId, itemName, enchants, mysteryEnch, mysteryMin, mysteryMax, exclusiveLine));
                count++;
            }
            log.info("Loaded " + count + " item(s) from " + file.getName());
            totalLoaded += count;
        }
        log.info("Total seasonal items loaded: " + totalLoaded);
    }

    public static ItemStack build(String itemId) {
        ItemDefinition def = ITEMS.get(itemId);
        if (def == null) return null;

        if (!NexoItems.exists(def.nexoId)) {
            RainfallPlugin.getInstance().getLogger()
                .severe("Nexo item not found: '" + def.nexoId + "' for reward '" + itemId + "'");
            return null;
        }

        ItemBuilder builder = NexoItems.itemFromId(def.nexoId);
        if (builder == null) return null;

        ItemStack baseStack = builder.build();
        if (baseStack == null) {
            RainfallPlugin.getInstance().getLogger()
                .severe("build() returned null for Nexo ID: '" + def.nexoId + "'");
            return null;
        }

        ItemStack item = baseStack.clone();

        // Ensure vanilla enchants are at the correct levels
        for (Map.Entry<Enchantment, Integer> entry : def.vanillaEnchants.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }

        // Set gradient item name and hide vanilla enchant lore
        ItemMeta meta = item.getItemMeta();
        meta.itemName(MM.deserialize(def.itemName).decoration(TextDecoration.ITALIC, false));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        // Apply AE mystery enchant at a random level
        int level = ThreadLocalRandom.current().nextInt(def.mysteryEnchantMinLevel, def.mysteryEnchantMaxLevel + 1);
        item = AEAPI.applyEnchant(def.mysteryEnchant, level, item);

        // Organize AE enchant lore
        item = AEAPI.organizeEnchants(item);

        // Build vanilla lore lines (gray ✦ format)
        List<Component> vanillaLines = new ArrayList<>();
        for (Map.Entry<Enchantment, String> entry : ENCHANT_DISPLAY.entrySet()) {
            Integer enchLevel = def.vanillaEnchants.get(entry.getKey());
            if (enchLevel == null) continue;
            String levelStr = enchLevel <= 10 ? ROMAN[enchLevel] : String.valueOf(enchLevel);
            vanillaLines.add(
                Component.text("✦ " + entry.getValue() + " " + levelStr)
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            );
        }

        // Append vanilla lines + exclusive line to lore
        ItemMeta finalMeta = item.getItemMeta();
        List<Component> lore = finalMeta.lore();
        if (lore == null) lore = new ArrayList<>();
        lore.addAll(vanillaLines);
        lore.add(Component.empty());
        lore.add(MM.deserialize(def.exclusiveLine).decoration(TextDecoration.ITALIC, false));
        finalMeta.lore(lore);
        item.setItemMeta(finalMeta);

        return item;
    }

    public static boolean exists(String itemId) {
        return ITEMS.containsKey(itemId);
    }
}
