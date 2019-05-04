package me.rxsto.minesleeper;

import me.rxsto.minesleeper.listeners.SleepListener;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MineSleeper extends JavaPlugin {

    private SleepListener sleepListener;
    private Map<World, BossBar> bossBars = new HashMap<>();

    public static void main(String[] args) {
        new MineSleeper();
    }

    @Override
    public void onEnable() {
        getLogger().info("Successfully enabled MineSleeper!");
        sleepListener = new SleepListener(this);
        getServer().getPluginManager().registerEvents(sleepListener, this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Successfully disabled MineSleeper!");
        sleepListener.onReload();
    }

    public Map<World, BossBar> getBossBars() {
        return bossBars;
    }
}
