package org.toni_4819.pvpPveLevelingSystem.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;
import org.toni_4819.pvpPveLevelingSystem.managers.XPManager;

public class LevelingExpansion extends PlaceholderExpansion {

    private final PvpPveLevelingSystem plugin;
    private final XPManager xpManager;

    public LevelingExpansion(PvpPveLevelingSystem plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    @Override
    public String getIdentifier() {
        return "pvppvelevelingsystem";
    }

    @Override
    public String getAuthor() {
        return "Toni_4819";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";

        switch (params.toLowerCase()) {
            case "xp": return String.valueOf(xpManager.getXP(player));
            case "level": return String.valueOf(xpManager.getLevel(player));
            case "remaining_xp": return String.valueOf(xpManager.getRemainingXP(player));
            case "bar": return xpManager.getXPBar(player);
            case "level_percent": return String.valueOf(xpManager.getLevelPercent(player));
            case "next_level_xp": return String.valueOf(xpManager.getNextLevelXP(player));
            case "next_level": return String.valueOf(xpManager.getNextLevel(player));
            default: return "";

        }
    }
}
