package com.blockymmc.rainfall.items;

import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public class ItemDefinition {

    public final String nexoId;
    public final String itemName;           // MiniMessage gradient string
    public final Map<Enchantment, Integer> vanillaEnchants;
    public final String mysteryEnchant;
    public final int mysteryEnchantMinLevel;
    public final int mysteryEnchantMaxLevel;
    public final String exclusiveLine;      // MiniMessage gradient string for bottom lore

    public ItemDefinition(String nexoId, String itemName,
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
}
