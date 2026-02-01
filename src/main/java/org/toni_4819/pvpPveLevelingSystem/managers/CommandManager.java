package org.toni_4819.pvpPveLevelingSystem.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;

import java.util.Map;

public class CommandManager implements CommandExecutor {

    private final PvpPveLevelingSystem plugin;
    private final XPManager xpManager;

    public CommandManager(PvpPveLevelingSystem plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getLangManager().getMessage("usage", Map.of(
                    "%command%", label
            )));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "xp":
                handleXP(sender, args, label);
                break;
            case "level":
                handleLevel(sender, args, label);
                break;
            case "reload":
                plugin.reloadConfig();
                plugin.getLangManager().loadLang();
                sender.sendMessage(plugin.getLangManager().getMessage("reload"));
                break;
            default:
                sender.sendMessage(plugin.getLangManager().getMessage("unknown-subcommand"));
                break;
        }
        return true;
    }

    private void handleXP(CommandSender sender, String[] args, String label) {
        if (args.length < 4) {
            sender.sendMessage(plugin.getLangManager().getMessage("xp-usage", Map.of(
                    "%command%", label
            )));
            return;
        }

        String action = args[1].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLangManager().getMessage("amount-number"));
            return;
        }

        Player target = Bukkit.getPlayer(args[3]);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("player-not-found"));
            return;
        }

        switch (action) {
            case "add":
                xpManager.addXP(target, amount);
                sender.sendMessage(plugin.getLangManager().getMessage("xp-added", Map.of(
                        "%amount%", String.valueOf(amount),
                        "%player%", target.getName()
                )));
                break;
            case "remove":
                int currentXP = xpManager.getXP(target);
                xpManager.addXP(target, -Math.min(amount, currentXP));
                sender.sendMessage(plugin.getLangManager().getMessage("xp-removed", Map.of(
                        "%amount%", String.valueOf(amount),
                        "%player%", target.getName()
                )));
                break;
            case "set":
                xpManager.setXP(target, amount);
                sender.sendMessage(plugin.getLangManager().getMessage("xp-set", Map.of(
                        "%amount%", String.valueOf(amount),
                        "%player%", target.getName()
                )));
                break;
            default:
                sender.sendMessage(plugin.getLangManager().getMessage("invalid-action"));
        }
    }

    private void handleLevel(CommandSender sender, String[] args, String label) {
        if (args.length < 4) {
            sender.sendMessage(plugin.getLangManager().getMessage("level-usage", Map.of(
                    "%command%", label
            )));
            return;
        }

        String action = args[1].toLowerCase();
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getLangManager().getMessage("amount-number"));
            return;
        }

        Player target = Bukkit.getPlayer(args[3]);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("player-not-found"));
            return;
        }

        switch (action) {
            case "add":
                xpManager.setLevel(target, xpManager.getLevel(target) + amount);
                sender.sendMessage(plugin.getLangManager().getMessage("level-added", Map.of(
                        "%amount%", String.valueOf(amount),
                        "%player%", target.getName()
                )));
                break;
            case "remove":
                xpManager.setLevel(target, Math.max(1, xpManager.getLevel(target) - amount));
                sender.sendMessage(plugin.getLangManager().getMessage("level-removed", Map.of(
                        "%amount%", String.valueOf(amount),
                        "%player%", target.getName()
                )));
                break;
            case "set":
                xpManager.setLevel(target, amount);
                sender.sendMessage(plugin.getLangManager().getMessage("level-set", Map.of(
                        "%amount%", String.valueOf(amount),
                        "%player%", target.getName()
                )));
                break;
            default:
                sender.sendMessage(plugin.getLangManager().getMessage("invalid-action"));
        }
    }
}
