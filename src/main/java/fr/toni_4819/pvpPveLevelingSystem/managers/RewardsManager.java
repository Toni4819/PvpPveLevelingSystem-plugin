package org.toni_4819.pvpPveLevelingSystem.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;

import java.util.List;
import java.util.Map;

public class RewardsManager {

    private final PvpPveLevelingSystem plugin;

    public RewardsManager(PvpPveLevelingSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes rewards for a given level if defined in config.
     */
    public void giveRewards(Player player, int level) {
        if (!plugin.getConfig().isConfigurationSection("rewards.levels")) return;

        String path = "rewards.levels." + level + ".commands";
        if (plugin.getConfig().isList(path)) {
            List<String> commands = plugin.getConfig().getStringList(path);

            for (String cmd : commands) {
                String parsed = cmd
                        .replace("%player%", player.getName())
                        .replace("%level%", String.valueOf(level));

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }

            player.sendMessage(plugin.getLangManager().getMessage("reward", Map.of(
                    "%level%", String.valueOf(level),
                    "%player%", player.getName()
            )));
        }
    }
}
