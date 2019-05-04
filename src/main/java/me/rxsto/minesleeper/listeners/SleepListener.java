package me.rxsto.minesleeper.listeners;

import me.rxsto.minesleeper.MineSleeper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Objects;
import java.util.stream.Stream;

public class SleepListener implements Listener {

    private final MineSleeper plugin;
    private int taskId = -1;
    private boolean instantMode;

    public SleepListener(MineSleeper plugin) {
        this.plugin = plugin;
         instantMode = plugin.getConfig().getDouble("playerCount") == 0;
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent e) {
        if (e.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            if (instantMode) {
                day(e.getPlayer().getWorld());
                return;
            }

            handleBedInteraction(e);
            broadcastActionBar(e.getPlayer().getWorld(), "actionbars.enter", e.getPlayer());
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent e) {
        if (instantMode) {
            return;
        }

        if (taskId != -2) {
            handleBedInteraction(e);
            broadcastActionBar(e.getPlayer().getWorld(), "actionbars.leave", e.getPlayer());
        } else if (getSleepingPlayers(e.getPlayer().getWorld()).count() == 0) {
            taskId = -1;
        }
    }

    private void broadcastActionBar(World world, String key, Player leftPlayer) {
        world.getPlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format(getMessage(key), leftPlayer.getName()))));
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        var world = player.getWorld();
        if (plugin.getBossBars().containsKey(world)) {
            plugin.getBossBars().get(world).addPlayer(player);
        }
    }

    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerQuit(PlayerQuitEvent e) {
        var player = e.getPlayer();
        var world = player.getWorld();
        if (plugin.getBossBars().containsKey(world)) {
            plugin.getBossBars().get(world).removePlayer(player);
        }
    }


    private void handleBedInteraction(PlayerEvent e) {
        var world = e.getPlayer().getWorld();
        var neededPlayers = calculateNeededPlayers(world);
        var sleepingPlayers = (int) getSleepingPlayers(world).count();

        if (e instanceof PlayerBedEnterEvent) {
            sleepingPlayers++;
        }

        updateBossbar(neededPlayers, sleepingPlayers, world);
    }

    private void updateBossbar(int neededPlayers, int sleepingPlayers, World world) {
        var bossBar = getBossBar(world);
        var progress = ((double) sleepingPlayers) / ((double) neededPlayers);
        bossBar.setProgress(progress);

        if (bossBar.getProgress() == 1.0) {
            finish(world, bossBar);
            return;
        }

        if (taskId != -2 && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        if (bossBar.getProgress() == 0.0) {
            dropBossBar(world, bossBar);
        } else {
            applyBossBar(bossBar, "waiting", neededPlayers, sleepingPlayers);
        }
    }

    private void dropBossBar(World world, BossBar bossBar) {
        bossBar.removeAll();
        bossBar.setVisible(false);
        Bukkit.removeBossBar(getKeyForWorld(world));
        plugin.getBossBars().remove(world);
    }

    private void finish(World world, BossBar bossBar) {
        applyBossBar(bossBar, "sleeping");
        taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(
                plugin,
                () -> {
                    taskId = -2;
                    applyBossBar(bossBar, "morning");
                    day(world);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> dropBossBar(world, bossBar), 2 * 20);
                }, 5 * 20
        );
    }

    private void day(World world) {
        world.setTime(0);

        if (plugin.getConfig().getBoolean("clearWeather")) {
            world.setStorm(false);
            world.setThundering(false);
        }
    }

    private NamespacedKey getKeyForWorld(World world) {
        return new NamespacedKey(plugin, world.getName() + "-sleepingBar");
    }

    private BossBar getBossBar(World world) {
        return plugin.getBossBars().computeIfAbsent(world, map -> {
            var bossBar = Bukkit.createBossBar(getKeyForWorld(world), "Preparing", BarColor.WHITE, BarStyle.SOLID);
            applyBossBar(bossBar, "waiting", 0, 0);
            world.getPlayers().forEach(bossBar::addPlayer);
            return bossBar;
        });
    }

    private Stream<? extends Player> getSleepingPlayers(World world) {
        return Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld().getName().equals(world.getName()) && player.isSleeping());
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    public void onReload() {
        Bukkit.getOnlinePlayers().stream().filter(HumanEntity::isSleeping).forEach(player -> {
            var previousGamemode = player.getGameMode();
            player.setGameMode(GameMode.SURVIVAL);
            player.damage(0.01);
            player.sendMessage(getMessage("reloadMessage"));
            if (player.getGameMode() != previousGamemode) {
                player.setGameMode(previousGamemode);
            }
        });
        plugin.getBossBars().entrySet().stream().forEach(entry -> dropBossBar(entry.getKey(), entry.getValue()));
    }

    private int calculateNeededPlayers(World world) {
        var percentage = plugin.getConfig().getDouble("playerCount");
        return (int) Math.ceil(Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld().equals(world)).count() * percentage);
    }

    private void applyBossBar(BossBar bossBar, String key) {
        applyBossBar(bossBar, key, -1, -1);
    }

    private void applyBossBar(BossBar bossBar, String key, int needed, int current) {
        var config = plugin.getConfig().getConfigurationSection("bossbars." + key);
        assert config != null;
        var text = Objects.requireNonNull(config.getString("text")).replace("&", "§");

        if (needed != -1 && current != -1) {
            text = String.format(text, current, needed);
        }

        bossBar.setTitle(text);
        bossBar.setColor(BarColor.valueOf(config.getString("color")));
    }

    private String getMessage(String key) {
        return Objects.requireNonNull(plugin.getConfig().getString(key)).replace("&", "§");
    }
}
