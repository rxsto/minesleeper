package me.rxsto.minesleeper;

import me.rxsto.minesleeper.listeners.SleepListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

public class MineSleeper extends JavaPlugin {

    private static MineSleeper instance;
    private ArrayList<String> sleepMessages;
    private ArrayList<UUID> sleepingPlayers;
    private HashMap<UUID, Future> sleepingFutures;

    @SuppressWarnings("WeakerAccess")
    public MineSleeper() {
        instance = this;
        sleepMessages = new ArrayList<>();
        sleepingPlayers = new ArrayList<>();
        sleepingFutures = new HashMap<>();
    }

    public static void main(String[] args) {
        new MineSleeper();
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

    public int getSleepingPlayerSize() {
        return sleepingPlayers.size();
    }

    public void addSleepingPlayer(UUID uuid) {
        sleepingPlayers.add(uuid);
    }

    public void removeSleepingPlayer(UUID uuid) {
        sleepingPlayers.remove(uuid);
    }

    public void addSleepingFuture(UUID uuid, Future future) {
        sleepingFutures.put(uuid, future);
    }

    public Future getSleepingFuture(UUID uuid) {
        return sleepingFutures.get(uuid);
    }

    public void cancelSleepingFuture(UUID uuid) {
        sleepingFutures.get(uuid).cancel(true);
    }

    public static MineSleeper getInstance() {
        return instance;
    }
}
