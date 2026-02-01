package org.toni_4819.pvpPveLevelingSystem.managers;

import org.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class StorageManager {
    private final PvpPveLevelingSystem plugin;

    public StorageManager(PvpPveLevelingSystem plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String provider = plugin.getConfig().getString("storage.provider", "sql");

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY," +
                    "xp INTEGER," +
                    "level INTEGER" +
                    ")");
            plugin.getLogger().info("Connected to storage using provider: " + provider);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to storage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        String provider = plugin.getConfig().getString("storage.provider", "sql");
        if (provider.equalsIgnoreCase("mysql")) {
            String host = plugin.getConfig().getString("mysql.host", "localhost");
            int port = plugin.getConfig().getInt("mysql.port", 3306);
            String database = plugin.getConfig().getString("mysql.database", "leveling");
            String user = plugin.getConfig().getString("mysql.username", "root");
            String pass = plugin.getConfig().getString("mysql.password", "");
            return DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                    user,
                    pass
            );
        } else {
            return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/data.db");
        }
    }

    public void close() {
        // Nothing to close here because connections are opened per query
        plugin.getLogger().info("Storage manager closed.");
    }
}
