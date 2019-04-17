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
import java.util.concurrent.TimeUnit;

public class SleepListener implements Listener {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressWarnings({"unused", "IntegerDivisionInFloatingPointContext"})
    @EventHandler
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        MineSleeper.getInstance().getLogger().info(event.getPlayer() + "");
        MineSleeper.getInstance().sleepingPlayer.add(event.getPlayer());
        MineSleeper.getInstance().getLogger().info("test");
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            if (MineSleeper.getInstance().getConfig().getInt("playerCount") == 1) {
                event.getPlayer().getWorld().getPlayers().forEach(player -> player.sendMessage(String.format(MineSleeper.getInstance().getSleepMessage(), event.getPlayer().getName())));
                executeSkip(event);
            } else {
                event.getPlayer().getWorld().getPlayers().forEach(player -> player.sendMessage(String.format("%s | %s/%s", String.format(MineSleeper.getInstance().getSleepMessage(), event.getPlayer().getName()), MineSleeper.getInstance().sleepingPlayer.size(), event.getPlayer().getWorld().getPlayers().size() * MineSleeper.getInstance().getConfig().getDouble("playerCount"))));
                if (MineSleeper.getInstance().sleepingPlayer.size() / event.getPlayer().getWorld().getPlayers().size() >= MineSleeper.getInstance().getConfig().getDouble("sleepingCount")) {
                    executeSkip(event);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerLeaveBed(PlayerBedLeaveEvent event) {
        MineSleeper.getInstance().sleepingPlayer.remove(event.getPlayer());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerKickedFromServer(PlayerKickEvent event) {
        MineSleeper.getInstance().sleepingPlayer.remove(event.getPlayer());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerLeaveServer(PlayerQuitEvent event) {
        MineSleeper.getInstance().sleepingPlayer.remove(event.getPlayer());
    }

    private void executeSkip(PlayerBedEnterEvent event) {
        Runnable runnableTask = () -> {
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
        };

        executorService.execute(runnableTask);
    }
}
