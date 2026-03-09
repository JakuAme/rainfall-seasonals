package com.blockymmc.rainfall;

import com.blockymmc.rainfall.items.SeasonalItemRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RainfallPlugin extends JavaPlugin {

    private static RainfallPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultCrates();
        SeasonalItemRegistry.loadFromFolder(new File(getDataFolder(), "crates"));
        getCommand("rainfall").setExecutor(new GiveCommand());
        getLogger().info("Rainfall enabled.");
    }

    private void saveDefaultCrates() {
        File cratesFolder = new File(getDataFolder(), "crates");
        if (!cratesFolder.exists()) {
            cratesFolder.mkdirs();
            saveResource("crates/valentine.yml", false);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Rainfall disabled.");
    }

    public static RainfallPlugin getInstance() {
        return instance;
    }
}
