package me.rxsto.minesleeper;

import me.rxsto.minesleeper.listeners.SleepListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class MineSleeper extends JavaPlugin {

    private static MineSleeper instance;
    public ArrayList<Player> sleepingPlayer;
    private ArrayList<String> sleepMessages;

    @SuppressWarnings("WeakerAccess")
    public MineSleeper() {
        instance = this;
        sleepMessages = new ArrayList<>();
    }

    public static void main(String[] args) {
        new MineSleeper();
    }

    public static MineSleeper getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        getLogger().info("Successfully enabled MineSleeper!");
        getServer().getPluginManager().registerEvents(new SleepListener(), this);
        saveDefaultConfig();
        Objects.requireNonNull(getConfig().getList("sleepMessages")).forEach(message -> sleepMessages.add(message.toString()));
    }

    @Override
    public void onDisable() {
        getLogger().info("Successfully disabled MineSleeper!");
    }

    public String getSleepMessage() {
        return sleepMessages.get(ThreadLocalRandom.current().nextInt(0, sleepMessages.size()));
    }
}
