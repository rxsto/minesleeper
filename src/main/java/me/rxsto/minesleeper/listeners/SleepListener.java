package me.rxsto.minesleeper.listeners;

import me.rxsto.minesleeper.MineSleeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SleepListener implements Listener {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressWarnings({"unused", "IntegerDivisionInFloatingPointContext"})
    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        MineSleeper.getInstance().addSleepingPlayer(event.getPlayer().getUniqueId());
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            if (MineSleeper.getInstance().getConfig().getDouble("playerCount") == 0) {
                event.getPlayer().getWorld().getPlayers().forEach(player -> player.sendMessage(String.format(MineSleeper.getInstance().getSleepMessage(), event.getPlayer().getName())));
                MineSleeper.getInstance().addSleepingFuture(event.getPlayer().getUniqueId(), executeSkip(event));
            } else {
                event.getPlayer().getWorld().getPlayers().forEach(player -> player.sendMessage(String.format("%s §7| §f%s/%s", String.format(MineSleeper.getInstance().getSleepMessage(), event.getPlayer().getName()), MineSleeper.getInstance().getSleepingPlayerSize(), Math.round(Math.ceil(event.getPlayer().getWorld().getPlayers().size() * MineSleeper.getInstance().getConfig().getDouble("playerCount"))))));
                if (MineSleeper.getInstance().getSleepingPlayerSize() / event.getPlayer().getWorld().getPlayers().size() >= MineSleeper.getInstance().getConfig().getDouble("sleepingCount")) {
                    MineSleeper.getInstance().addSleepingFuture(event.getPlayer().getUniqueId(), executeSkip(event));
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event) {
        if (!MineSleeper.getInstance().getSleepingFuture(event.getPlayer().getUniqueId()).isDone())
            MineSleeper.getInstance().cancelSleepingFuture(event.getPlayer().getUniqueId());
        MineSleeper.getInstance().removeSleepingPlayer(event.getPlayer().getUniqueId());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerKickedFromServer(PlayerKickEvent event) {
        if (!MineSleeper.getInstance().getSleepingFuture(event.getPlayer().getUniqueId()).isDone())
            MineSleeper.getInstance().cancelSleepingFuture(event.getPlayer().getUniqueId());
        MineSleeper.getInstance().removeSleepingPlayer(event.getPlayer().getUniqueId());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerLeaveServer(PlayerQuitEvent event) {
        if (!MineSleeper.getInstance().getSleepingFuture(event.getPlayer().getUniqueId()).isDone())
            MineSleeper.getInstance().cancelSleepingFuture(event.getPlayer().getUniqueId());
        MineSleeper.getInstance().removeSleepingPlayer(event.getPlayer().getUniqueId());
    }

    private Future executeSkip(PlayerBedEnterEvent event) {
        return executorService.submit(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(5000);
                event.getPlayer().getWorld().setTime(4000);
                if (MineSleeper.getInstance().getConfig().getBoolean("clearWeather")) {
                    event.getPlayer().getWorld().setThundering(false);
                    event.getPlayer().getWorld().setStorm(false);
                }
            } catch (InterruptedException e) {
                MineSleeper.getInstance().getLogger().warning(e.getLocalizedMessage());
            }
        });
    }
}
