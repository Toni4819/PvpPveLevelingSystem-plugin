package org.toni_4819.pvpPveLevelingSystem.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;
import org.toni_4819.pvpPveLevelingSystem.managers.RewardsManager;
import org.toni_4819.pvpPveLevelingSystem.managers.XPManager;

public class XPListener implements Listener {

    private final PvpPveLevelingSystem plugin;
    private final XPManager xpManager;

    public XPListener(PvpPveLevelingSystem plugin, XPManager xpManager, RewardsManager rewardsManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player player) {
            EntityType type = event.getEntityType();
            int xp = plugin.getConfig().getInt("xp-sources." + type.name().toLowerCase(), 0);
            xpManager.addXP(player, xp);
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getXpManager().loadPlayer(player);
        plugin.getLogger().info("Loaded data for player: " + player.getName());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getXpManager().savePlayer(player);
        plugin.getLogger().info("Saved data for player on quit: " + player.getName());
    }

}
