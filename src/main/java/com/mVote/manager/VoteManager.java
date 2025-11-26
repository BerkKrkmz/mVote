package com.mVote.manager;

import com.mVote.Main;
import com.mVote.storage.IStorage;
import com.mVote.storage.SqliteStorage;
import com.mVote.storage.YamlStorage;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class VoteManager {
    private final Main plugin;
    private IStorage storage;
    private BukkitTask resetTask;

    public VoteManager(Main plugin) {
        this.plugin = plugin;
        setupStorage();
        startDailyResetTask();
    }

    private void setupStorage() {
        String type = plugin.getConfig().getString("storage-type", "YAML");
        if (type.equalsIgnoreCase("SQLITE")) {
            this.storage = new SqliteStorage(plugin);
        } else {
            this.storage = new YamlStorage(plugin);
        }
        this.storage.setup();
    }

    public boolean hasVoted(UUID uuid) {
        return storage.hasVoted(uuid);
    }

    public boolean hasVotedByName(String playerName) {
        return storage.hasVotedByName(playerName);
    }

    public void addVote(UUID uuid) {
        storage.addVote(uuid);
    }

    public void addVoteByName(String playerName) {
        storage.addVoteByName(playerName);
    }

    public void removeVoteByName(String playerName) { // 👈 EKSİK OLAN METOT EKLENDİ!
        storage.removeVoteByName(playerName);
    }

    public void reload() {
        if (resetTask != null) {
            resetTask.cancel();
            plugin.getLogger().info("Eski zamanlayıcı görevi durduruldu.");
        }

        storage.reload();

        startDailyResetTask();

        plugin.getLogger().info("Config/Storage yenilendi ve zamanlayıcı yeniden başlatıldı.");
    }

    public void cancelTask() {
        if (resetTask != null) {
            resetTask.cancel();
        }
    }

    private void startDailyResetTask() {
        int resetHour = plugin.getConfig().getInt("reset-time", 7);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextResetTime = now
                .withHour(resetHour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        if (now.compareTo(nextResetTime) >= 0) {
            nextResetTime = nextResetTime.plusDays(1);
        }

        long secondsUntilReset = Duration.between(now, nextResetTime).getSeconds();
        long initialDelay = secondsUntilReset * 20L;
        long period = 24 * 60 * 60 * 20L;

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                storage.clearVotes();
                plugin.getLogger().info("Günlük oy kaydı sıfırlandı. (Saat: " + resetHour + ":00)");
            }
        };

        this.resetTask = runnable.runTaskTimer(plugin, initialDelay, period);
    }

    public void close() {
        storage.close();
    }
}