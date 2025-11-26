package com.mVote.command;

import com.mVote.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MVoteCommand implements CommandExecutor {
    private final Main plugin;

    public MVoteCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String adminPerm = plugin.getConfig().getString("permissions.admin-permission", "mvote.admin");
        if (!sender.hasPermission(adminPerm) && !(sender instanceof Player && ((Player) sender).isOp())) {
            sender.sendMessage(ChatColor.RED + "Bu komutu kullanmaya yetkiniz yok.");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPluginConfig();
            plugin.getVoteManager().reload();

            sender.sendMessage(ChatColor.GREEN + "[MVote] Config ve Oy verileri basariyla yeniden yuklendi.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "/mvote reload - Ayarlari ve oy.yml verilerini yeniler.");
        return true;
    }
}