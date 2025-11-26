package com.mVote.storage;

import com.mVote.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class YamlStorage implements IStorage {
    private final Main plugin;
    private File voteFile;
    private FileConfiguration voteConfig;

    private final Set<UUID> dailyVoters = new HashSet<>();
    private final Set<String> pendingVotesByName = new HashSet<>();

    public YamlStorage(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        plugin.getDataFolder().mkdirs();

        // 1. Dosya adını SENİN İSTEDİĞİN GİBİ vote.yml yaptık
        voteFile = new File(plugin.getDataFolder(), "vote.yml");

        if (!voteFile.exists()) {
            try {
                // Önce JAR içinden kopyalamayı dene
                plugin.saveResource("vote.yml", false);
            } catch (Exception e) {
                // Eğer JAR içinde yoksa (Unuttuysan), BOŞ BİR TANE OLUŞTUR (Hata vermez artık)
                try {
                    voteFile.createNewFile();
                } catch (IOException ex) {
                    plugin.getLogger().severe("vote.yml oluşturulamadı!");
                    ex.printStackTrace();
                }
            }
        }

        voteConfig = YamlConfiguration.loadConfiguration(voteFile);

        loadVotersFromConfig();
        plugin.getLogger().info("YAML (vote.yml) yüklendi. Kayıtlı oy: " + (dailyVoters.size() + pendingVotesByName.size()));
    }

    @Override
    public void addVote(UUID uuid) {
        dailyVoters.add(uuid);
        saveVotersToConfig();
    }

    @Override
    public void addVoteByName(String username) {
        pendingVotesByName.add(username.toLowerCase());
        saveVotersToConfig();
    }

    @Override
    public void removeVoteByName(String username) {
        if (pendingVotesByName.remove(username.toLowerCase())) {
            saveVotersToConfig();
        }
    }

    @Override
    public void reload() {
        if (!voteFile.exists()) {
            setup();
        }
        voteConfig = YamlConfiguration.loadConfiguration(voteFile);
        loadVotersFromConfig();
        plugin.getLogger().info("vote.yml yeniden yüklendi.");
    }

    @Override
    public boolean hasVoted(UUID uuid) {
        return dailyVoters.contains(uuid);
    }

    @Override
    public boolean hasVotedByName(String username) {
        return pendingVotesByName.contains(username.toLowerCase());
    }

    @Override
    public void clearVotes() {
        dailyVoters.clear();
        pendingVotesByName.clear();
        saveVotersToConfig();
    }

    private void loadVotersFromConfig() {
        dailyVoters.clear();
        pendingVotesByName.clear();

        List<String> uuidList = voteConfig.getStringList("daily-voters");
        for (String s : uuidList) {
            try {
                dailyVoters.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }

        List<String> nameList = voteConfig.getStringList("pending-votes-by-name");
        pendingVotesByName.addAll(nameList.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));
    }

    private void saveVotersToConfig() {
        List<String> uuidList = dailyVoters.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        voteConfig.set("daily-voters", uuidList);

        voteConfig.set("pending-votes-by-name", new ArrayList<>(pendingVotesByName));

        try {
            voteConfig.save(voteFile);
        } catch (IOException e) {
            plugin.getLogger().severe("vote.yml dosyasına yazılamadı!");
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
    }
}