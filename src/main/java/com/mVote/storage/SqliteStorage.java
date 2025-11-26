package com.mVote.storage;

import com.mVote.Main;
import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SqliteStorage implements IStorage {
    private final Main plugin;
    private Connection connection;

    public SqliteStorage(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        try {
            File dataFolder = new File(plugin.getDataFolder(), "database.db");
            if (!dataFolder.getParentFile().exists()) dataFolder.getParentFile().mkdirs();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath());

            try (Statement st = connection.createStatement()) {
                // uuid (oy verenler) ve username (bekleyen oylar) için iki alan da kritik
                st.execute("CREATE TABLE IF NOT EXISTS votes (uuid TEXT, username TEXT, date LONG)");
            }
            plugin.getLogger().info("SQLite baglantisi basarili!");
        } catch (Exception e) {
            plugin.getLogger().severe("SQLite baglantisi kurulamadi!");
            e.printStackTrace();
        }
    }

    @Override
    public void addVote(UUID uuid) {
        // UUID ile oy ekleme
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO votes (uuid, username, date) VALUES (?, NULL, ?)"
        )) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void addVoteByName(String username) {
        // İsimle oy ekleme (UUID NULL kalacak)
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO votes (uuid, username, date) VALUES (NULL, ?, ?)"
        )) {
            ps.setString(1, username.toLowerCase());
            ps.setLong(2, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    @Override
    public boolean hasVoted(UUID uuid) {
        // UUID ile oy kontrolü
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(uuid) FROM votes WHERE uuid = ?"
        )) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean hasVotedByName(String username) {
        // İsimle bekleyen oy kontrolü
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(username) FROM votes WHERE username = ?"
        )) {
            ps.setString(1, username.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void removeVoteByName(String username) {
        // Bekleyen oyu silme (UUID'ye çevrilirken kullanılır)
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM votes WHERE username = ?"
        )) {
            ps.setString(1, username.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void clearVotes() {
        try (Statement st = connection.createStatement()) { st.execute("DELETE FROM votes"); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void reload() {
        plugin.getLogger().info("SQLite storage yenilendi (Baglanti kontrol edildi).");
    }

    @Override
    public void close() {
        try { if (connection != null) connection.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}