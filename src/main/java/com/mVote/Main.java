package com.mVote;


import com.mVote.listener.RestrictionListener;
import com.mVote.listener.VoteListener;
import com.mVote.manager.VoteManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private VoteManager voteManager;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Config yoksa oluşturur

        // Manager'ı başlat
        this.voteManager = new VoteManager(this);

        // Listener'ları kaydet
        getServer().getPluginManager().registerEvents(new RestrictionListener(this), this);
        getServer().getPluginManager().registerEvents(new VoteListener(this), this); //

        getCommand("mvote").setExecutor(new com.mVote.command.MVoteCommand(this)); // Komutu kaydet
        getLogger().info("MelezVoteManager aktif edildi! Depolama: " + getConfig().getString("storage-type"));
    }

    public void reloadPluginConfig() {
        reloadConfig();
    }

    @Override
    public void onDisable() {
        if (voteManager != null) {
            voteManager.close();
            voteManager.cancelTask();
        }
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }
}