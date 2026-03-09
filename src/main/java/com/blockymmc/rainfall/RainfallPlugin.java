package com.blockymmc.rainfall;

import com.blockymmc.rainfall.items.SeasonalItemRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RainfallPlugin extends JavaPlugin {

    private static RainfallPlugin instance;
    private SeasonalItemRegistry registry;

    @Override
    public void onEnable() {
        instance = this;
        registry = new SeasonalItemRegistry(getLogger());
        saveDefaultCrates();
        registry.load(new File(getDataFolder(), "crates"));
        getCommand("rainfall").setExecutor(new GiveCommand(registry));
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

    public SeasonalItemRegistry getRegistry() {
        return registry;
    }
}
