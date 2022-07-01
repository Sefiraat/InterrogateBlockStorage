package dev.sefiraat.interrogateblockstorage;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Interrogate extends JavaPlugin implements SlimefunAddon {
    private static Interrogate instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        new DispatchManager(this);
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return null;
    }

    public static Interrogate getInstance() {
        return instance;
    }
}
