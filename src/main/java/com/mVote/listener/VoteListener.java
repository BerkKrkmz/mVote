package com.mVote.listener;

import com.mVote.Main;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class VoteListener implements Listener {
    private final Main plugin;

    public VoteListener(Main plugin) {
        this.plugin = plugin;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        String playerName = event.getVote().getUsername();
        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
        String successMessage = plugin.getConfig().getString("messages.vote-received", "&aOyun kaydedildi! Tesekkurler.");

        // Oyuncu UUID'si alınabiliyorsa veya online ise, oy kaydını UUID ile yap.
        // Bu, bekleyen isim listesi hatasını engeller.
        if (offPlayer.getUniqueId() != null && (offPlayer.hasPlayedBefore() || offPlayer.isOnline())) {
            plugin.getVoteManager().addVote(offPlayer.getUniqueId());
            Player onlinePlayer = offPlayer.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayer.sendMessage(color(successMessage));
            }
        } else {
            // Oyuncu hiç girmemişse ismi küçük harfle kaydet
            plugin.getVoteManager().addVoteByName(playerName);
            plugin.getLogger().info("[MVote] " + playerName + " sunucuya hic girmemis, isim ile kaydedildi.");
        }
    }

    /**
     * Oyuncu sunucuya girdiğinde bekleyen oyları (isimle kaydedilenleri) UUID'ye çevirir.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String lowerName = player.getName().toLowerCase();

        // İsimle bekleyen oy varsa
        if (plugin.getVoteManager().hasVotedByName(lowerName)) {

            // 1. İsim kaydını sil
            plugin.getVoteManager().removeVoteByName(lowerName);

            // 2. UUID kaydını ekle (Bu an itibarıyla sohbete yazabilir)
            plugin.getVoteManager().addVote(player.getUniqueId());

            plugin.getLogger().info("[MVote] " + player.getName() + " icin bekleyen oy UUID'ye cevrildi ve aktif edildi.");

            // Oyuncuya bilgi ver
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(color(plugin.getConfig().getString("messages.vote-activated", "&aDaha once verdigin oy aktif edildi!")));
                }
            }.runTaskLater(plugin, 40L);
        }
    }
}