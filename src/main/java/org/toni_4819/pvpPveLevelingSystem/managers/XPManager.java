package org.toni_4819.pvpPveLevelingSystem.managers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XPManager {
    private final PvpPveLevelingSystem plugin;
    private final LuckPerms luckPerms; // can be null
    private final Map<Player, Integer> xpMap = new HashMap<>();
    private final Map<Player, Integer> levelMap = new HashMap<>();

    public XPManager(PvpPveLevelingSystem plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    public void addXP(Player player, int baseXP) {
        double multiplier = getGroupMultiplier(player);
        int xp = (int) (baseXP * multiplier);

        xpMap.put(player, xpMap.getOrDefault(player, 0) + xp);

        int currentXP = xpMap.get(player);
        int currentLevel = levelMap.getOrDefault(player, 1);
        int requiredXP = getRequiredXP(currentLevel);

        if (currentXP >= requiredXP) {
            levelMap.put(player, currentLevel + 1);
            xpMap.put(player, currentXP - requiredXP);

            player.sendMessage(plugin.getLangManager().getMessage("level-up", Map.of(
                    "%level%", String.valueOf(currentLevel + 1)
            )));

            if (plugin.getConfig().isList("generic-level-command")) {
                List<String> commands = plugin.getConfig().getStringList("generic-level-command");
                if (!commands.isEmpty()) {
                    for (String cmd : commands) {
                        String parsed = cmd
                                .replace("%player%", player.getName())
                                .replace("%level%", String.valueOf(currentLevel + 1));
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
                    }
                }
            }

            plugin.getRewardsManager().giveRewards(player, currentLevel + 1);
        }

        savePlayer(player);
    }

    public int getXP(Player player) {
        return xpMap.getOrDefault(player, 0);
    }

    public int getLevel(Player player) {
        return levelMap.getOrDefault(player, 1);
    }

    public int getRemainingXP(Player player) {
        int currentLevel = getLevel(player);
        int requiredXP = getRequiredXP(currentLevel);
        return requiredXP - getXP(player);
    }

    public String getXPBar(Player player) {
        int length = plugin.getConfig().getInt("bar.length", 5);
        String full = plugin.getConfig().getString("bar.full", "&6-");
        String empty = plugin.getConfig().getString("bar.empty", "&7-");

        int currentXP = getXP(player);
        int requiredXP = getRequiredXP(getLevel(player));
        double progress = (double) currentXP / requiredXP;

        int filled = (int) Math.round(progress * length);
        return full.repeat(filled) + empty.repeat(length - filled);
    }

    private double getGroupMultiplier(Player player) {
        if (luckPerms == null) {
            return plugin.getConfig().getDouble("group-xp-multipliers.default", 1.0);
        }
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return plugin.getConfig().getDouble("group-xp-multipliers.default", 1.0);
        }
        String group = user.getPrimaryGroup();
        return plugin.getConfig().getDouble("group-xp-multipliers." + group,
                plugin.getConfig().getDouble("group-xp-multipliers.default", 1.0));
    }

    private int getRequiredXP(int level) {
        boolean useMultiplier = plugin.getConfig().getBoolean("levels-xp.use-multiplier", true);
        if (useMultiplier) {
            int baseXP = plugin.getConfig().getInt("levels-xp.base-xp", 10);
            double multiplier = plugin.getConfig().getDouble("levels-xp.multiplier", 0.1);
            return (int) (baseXP + (level * multiplier * baseXP));
        } else {
            return plugin.getConfig().getInt("levels-xp." + level, level * 100);
        }
    }

    public void setXP(Player player, int amount) {
        xpMap.put(player, Math.max(0, amount));
        savePlayer(player);
    }

    public void setLevel(Player player, int level) {
        levelMap.put(player, Math.max(1, level));
        savePlayer(player);
    }

    public double getLevelPercent(Player player) {
        int currentXP = getXP(player);
        int requiredXP = getRequiredXP(getLevel(player));
        if (requiredXP == 0) return 0.0;
        return Math.round((double) currentXP / requiredXP * 100.0);
    }

    public int getNextLevelXP(Player player) {
        return getRequiredXP(getLevel(player));
    }

    public int getNextLevel(Player player) {
        return getLevel(player) + 1;
    }

    public void loadPlayer(Player player) {
        try (Connection conn = plugin.getStorageManager().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT xp, level FROM players WHERE uuid = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                xpMap.put(player, rs.getInt("xp"));
                levelMap.put(player, rs.getInt("level"));
            } else {
                xpMap.put(player, 0);
                levelMap.put(player, 1);
                savePlayer(player);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(Player player) {
        try (Connection conn = plugin.getStorageManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO players (uuid, xp, level) VALUES (?, ?, ?)"
             )) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, getXP(player));
            ps.setInt(3, getLevel(player));
            ps.executeUpdate();

            plugin.getLogger().info("Player data saved: " + player.getName() +
                    " (XP=" + getXP(player) + ", Level=" + getLevel(player) + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
