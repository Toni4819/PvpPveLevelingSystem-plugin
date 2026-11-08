package fr.toni_4819.pvpPveLevelingSystem.gui;

import fr.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** /levels [page] */
public class LevelsCommand implements CommandExecutor {

    private final PvpPveLevelingSystem plugin;

    public LevelsCommand(PvpPveLevelingSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().getMessage("players-only"));
            return true;
        }

        if (args.length > 0) {
            try {
                plugin.getGuiManager().open(player, Integer.parseInt(args[0]) - 1);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getLangManager().getMessage("amount-number"));
            }
            return true;
        }

        plugin.getGuiManager().open(player);
        return true;
    }
}
