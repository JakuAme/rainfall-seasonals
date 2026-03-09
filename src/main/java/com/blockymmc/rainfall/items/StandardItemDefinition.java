package com.blockymmc.rainfall.items;

import com.blockymmc.rainfall.RainfallPlugin;
import net.advancedplugins.ae.api.AEAPI;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StandardItemDefinition implements ItemDefinition {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private final String nexoId;
    private final String itemName;
    private final Map<Enchantment, Integer> vanillaEnchants; // LinkedHashMap to preserve YAML order
    private final String mysteryEnchant;
    private final int mysteryEnchantMinLevel;
    private final int mysteryEnchantMaxLevel;
    private final String exclusiveLine;

    public StandardItemDefinition(String nexoId, String itemName,
                                  Map<Enchantment, Integer> vanillaEnchants,
                                  String mysteryEnchant, int mysteryEnchantMinLevel, int mysteryEnchantMaxLevel,
                                  String exclusiveLine) {
        this.nexoId = nexoId;
        this.itemName = itemName;
        this.vanillaEnchants = vanillaEnchants;
        this.mysteryEnchant = mysteryEnchant;
        this.mysteryEnchantMinLevel = mysteryEnchantMinLevel;
        this.mysteryEnchantMaxLevel = mysteryEnchantMaxLevel;
        this.exclusiveLine = exclusiveLine;
    }

    @Override
    public ItemStack build() {
        if (!NexoItems.exists(nexoId)) {
            RainfallPlugin.getInstance().getLogger().severe("Nexo item not found: '" + nexoId + "'");
            return null;
        }

        ItemBuilder builder = NexoItems.itemFromId(nexoId);
        if (builder == null) return null;

        ItemStack item = builder.build();
        if (item == null) {
            RainfallPlugin.getInstance().getLogger().severe("build() returned null for Nexo ID: '" + nexoId + "'");
            return null;
        }

        item = item.clone();

        for (Map.Entry<Enchantment, Integer> entry : vanillaEnchants.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }

        ItemMeta meta = item.getItemMeta();
        meta.itemName(MM.deserialize(itemName).decoration(TextDecoration.ITALIC, false));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        int level = EnchantTierRoller.roll(mysteryEnchantMaxLevel);
        item = AEAPI.applyEnchant(mysteryEnchant, level, item);
        item = AEAPI.organizeEnchants(item);

        if (level == mysteryEnchantMaxLevel) {
            ItemMeta aeMeta = item.getItemMeta();
            List<Component> lore = aeMeta.lore();
            if (lore != null) {
                LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
                lore.replaceAll(line -> AEAPI.isEnchantLine(legacy.serialize(line))
                    ? line.decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false)
                    : line);
                aeMeta.lore(lore);
                item.setItemMeta(aeMeta);
            }
        }

        List<Component> vanillaLines = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : vanillaEnchants.entrySet()) {
            int lvl = entry.getValue();
            String levelStr = lvl <= 10 ? ROMAN[lvl] : String.valueOf(lvl);
            vanillaLines.add(
                Component.text("✦ " + enchantDisplayName(entry.getKey()) + " " + levelStr)
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            );
        }

        ItemMeta finalMeta = item.getItemMeta();
        List<Component> lore = finalMeta.lore();
        if (lore == null) lore = new ArrayList<>();
        lore.addAll(vanillaLines);
        lore.add(Component.empty());
        lore.add(MM.deserialize(exclusiveLine).decoration(TextDecoration.ITALIC, false));
        finalMeta.lore(lore);
        item.setItemMeta(finalMeta);

        return item;
    }

    private static String enchantDisplayName(Enchantment ench) {
        return Arrays.stream(ench.getKey().getKey().split("_"))
            .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
            .collect(Collectors.joining(" "));
    }
}
