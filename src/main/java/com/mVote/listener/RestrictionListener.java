package com.mVote.listener;

import com.mVote.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class RestrictionListener implements Listener {
    private final Main plugin;

    public RestrictionListener(Main plugin) {
        this.plugin = plugin;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private boolean canBypass(Player player) {
        boolean opBypass = plugin.getConfig().getBoolean("permissions.op-bypass", true);
        String bypassPerm = plugin.getConfig().getString("permissions.bypass-permission", "mvote.bypass");

        if (opBypass && player.isOp()) return true;
        if (player.hasPermission(bypassPerm)) return true;

        return false;
    }

    /**
     * Oyuncunun aktif (UUID) veya bekleyen (İsim) oyu var mı kontrolü
     */
    private boolean playerHasActiveVote(Player player) {
        // İsim kontrolü için oyuncu adını küçük harfe çeviriyoruz.
        String lowerName = player.getName().toLowerCase(); // 👈 YENİ EKLENDİ

        // 1. UUID ile kontrol (normal oy)
        if (plugin.getVoteManager().hasVoted(player.getUniqueId())) {
            return true;
        }
        // 2. İsim ile kontrol (bekleyen oy)
        if (plugin.getVoteManager().hasVotedByName(lowerName)) { // 👈 KÜÇÜK HARF KONTROLÜ
            return true;
        }
        return false;
    }


    // 1. SOHBET KISITLAMASI
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("restrictions.chat")) return;

        Player player = event.getPlayer();
        if (canBypass(player)) return;

        if (!playerHasActiveVote(player)) {
            event.setCancelled(true);
            player.sendMessage(color(plugin.getConfig().getString("messages.no-vote-chat")));
        }
    }

    // 2. TAKAS (SHOPKEEPER) KISITLAMASI
    @EventHandler
    public void onTrade(PlayerInteractEntityEvent event) {
        if (!plugin.getConfig().getBoolean("restrictions.trade")) return;

        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            if (villager.getScoreboardTags().contains("Shopkeeper")) {
                Player player = event.getPlayer();
                if (canBypass(player)) return;

                if (!playerHasActiveVote(player)) {
                    event.setCancelled(true);
                    player.sendMessage(color(plugin.getConfig().getString("messages.no-vote-trade")));
                }
            }
        }
    }

    // 3. KASA (CRATE) KISITLAMASI
    @EventHandler
    public void onCrateOpen(PlayerInteractEvent event) {
        if (!plugin.getConfig().getBoolean("restrictions.crates")) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material type = event.getClickedBlock().getType();
        List<String> blockedBlocks = plugin.getConfig().getStringList("restrictions.crate-blocks");

        if (blockedBlocks.contains(type.name())) {
            Player player = event.getPlayer();
            if (canBypass(player)) return;

            if (!playerHasActiveVote(player)) {
                event.setCancelled(true);
                player.sendMessage(color(plugin.getConfig().getString("messages.no-vote-crate")));
            }
        }
    }
}