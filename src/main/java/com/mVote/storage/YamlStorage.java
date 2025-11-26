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
    private File oyFile;
    private FileConfiguration oyConfig;

    private final Set<UUID> dailyVoters = new HashSet<>();
    private final Set<String> pendingVotesByName = new HashSet<>();

    public YamlStorage(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        // dataFolder ve vote.yml yükleme
        plugin.getDataFolder().mkdirs();
        oyFile = new File(plugin.getDataFolder(), "vote.yml");
        if (!oyFile.exists()) {
            plugin.saveResource("vote.yml", false);
        }
        oyConfig = YamlConfiguration.loadConfiguration(oyFile);

        loadVotersFromConfig();
        plugin.getLogger().info("YAML depolama yuklendi. Kayitli oy: " + (dailyVoters.size() + pendingVotesByName.size()));
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
    public void removeVoteByName(String username) { // 👈 EKSİK OLAN VE EKLENEN METOT
        if (pendingVotesByName.remove(username.toLowerCase())) {
            saveVotersToConfig();
            plugin.getLogger().info("Bekleyen oy kaydi silindi (Isim): " + username);
        }
    }


    @Override
    public void reload() {
        oyConfig = YamlConfiguration.loadConfiguration(oyFile);
        loadVotersFromConfig();
        plugin.getLogger().info("vote.yml yeniden yüklendi ve cache güncellendi.");
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

        List<String> uuidList = oyConfig.getStringList("daily-voters");
        for (String s : uuidList) {
            try {
                dailyVoters.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {}
        }

        List<String> nameList = oyConfig.getStringList("pending-votes-by-name");
        pendingVotesByName.addAll(nameList.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));
    }

    private void saveVotersToConfig() {
        List<String> uuidList = dailyVoters.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        oyConfig.set("daily-voters", uuidList);

        oyConfig.set("pending-votes-by-name", new ArrayList<>(pendingVotesByName));

        try {
            oyConfig.save(oyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("vote.yml kaydedilemedi!");
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
    }
}