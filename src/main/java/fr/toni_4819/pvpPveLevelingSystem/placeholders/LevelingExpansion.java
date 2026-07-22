package fr.toni_4819.pvpPveLevelingSystem.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import fr.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;
import fr.toni_4819.pvpPveLevelingSystem.managers.XPManager;

public class LevelingExpansion extends PlaceholderExpansion {

    private final PvpPveLevelingSystem plugin;
    private final XPManager xpManager;

    public LevelingExpansion(PvpPveLevelingSystem plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "pvppvelevelingsystem";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Toni_4819";
    }

    @Override
    public @NonNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NonNull String params) {
        if (player == null) return "";

        return switch (params.toLowerCase()) {
            case "xp" -> String.valueOf(xpManager.getXP(player));
            case "level" -> String.valueOf(xpManager.getLevel(player));
            case "remaining_xp" -> String.valueOf(xpManager.getRemainingXP(player));
            case "bar" -> xpManager.getXPBar(player);
            case "level_percent" -> String.valueOf(xpManager.getLevelPercent(player));
            case "next_level_xp" -> String.valueOf(xpManager.getNextLevelXP(player));
            case "next_level" -> String.valueOf(xpManager.getNextLevel(player));
            default -> "";
        };
    }
}
