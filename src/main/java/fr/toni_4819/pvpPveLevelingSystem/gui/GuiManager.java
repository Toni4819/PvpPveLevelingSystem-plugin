package fr.toni_4819.pvpPveLevelingSystem.gui;

import fr.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;
import fr.toni_4819.pvpPveLevelingSystem.managers.XPManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the levels menu from gui.yml: one item per level with the XP left to reach it and its rewards.
 */
public class GuiManager {

    private final PvpPveLevelingSystem plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    private FileConfiguration gui;
    private List<Integer> levelSlots = List.of();
    private int size;

    public GuiManager(PvpPveLevelingSystem plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "gui.yml");
        if (!file.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        gui = YamlConfiguration.loadConfiguration(file);

        size = Math.clamp(gui.getInt("rows", 6), 1, 6) * 9;
        levelSlots = parseSlots(gui.getStringList("level-slots"), size);
        if (levelSlots.isEmpty()) {
            plugin.getLogger().warning("gui.yml: 'level-slots' is empty or invalid, the levels menu cannot be opened.");
        }
    }

    /** Opens the page that contains the player's next level. */
    public void open(Player player) {
        if (levelSlots.isEmpty()) return;
        int next = plugin.getXpManager().getLevel(player) + 1;
        int index = Math.clamp(next - firstLevel(), 0, levelCount() - 1);
        open(player, index / levelSlots.size());
    }

    public void open(Player player, int page) {
        if (levelSlots.isEmpty()) return;
        XPManager xp = plugin.getXpManager();

        int perPage = levelSlots.size();
        int pages = Math.max(1, (levelCount() + perPage - 1) / perPage);
        page = Math.clamp(page, 0, pages - 1);

        Map<String, String> base = basePlaceholders(player, xp, page, pages);

        LevelsMenu holder = new LevelsMenu(page);
        Inventory inventory = Bukkit.createInventory(holder, size, text(player, gui.getString("title", "Levels"), base));
        holder.setInventory(inventory);

        // Filler
        ConfigurationSection filler = gui.getConfigurationSection("filler");
        if (filler != null && filler.getBoolean("enabled", true)) {
            ItemStack item = buildItem(player, filler, base, List.of());
            for (int i = 0; i < size; i++) inventory.setItem(i, item);
        }

        // Level items
        int first = firstLevel();
        int last = first + levelCount() - 1;
        int currentLevel = xp.getLevel(player);
        for (int i = 0; i < perPage; i++) {
            int level = first + page * perPage + i;
            if (level > last) break;
            inventory.setItem(levelSlots.get(i), buildLevelItem(player, xp, level, currentLevel, base));
        }

        // Buttons
        placeButton(player, holder, inventory, "info", null, base);
        placeButton(player, holder, inventory, "close", LevelsMenu.Action.CLOSE, base);
        if (page > 0) {
            placeButton(player, holder, inventory, "previous-page", LevelsMenu.Action.PREVIOUS_PAGE, base);
        }
        if (page < pages - 1) {
            placeButton(player, holder, inventory, "next-page", LevelsMenu.Action.NEXT_PAGE, base);
        }

        player.openInventory(inventory);
    }

    private ItemStack buildLevelItem(Player player, XPManager xp, int level, int currentLevel, Map<String, String> base) {
        String state = level <= currentLevel ? "unlocked" : level == currentLevel + 1 ? "next" : "locked";

        // State item, then per-level overrides on top of it.
        ConfigurationSection section = new YamlConfiguration();
        copyInto(gui.getConfigurationSection("levels." + state), section);
        copyInto(gui.getConfigurationSection("level-overrides." + level), section);

        Map<String, String> placeholders = new HashMap<>(base);
        placeholders.put("%level%", String.valueOf(level));
        placeholders.put("%xp_remaining%", String.valueOf(xp.getXPToReachLevel(player, level)));
        placeholders.put("%xp_required%", String.valueOf(level <= 1 ? 0 : xp.getRequiredXP(level - 1)));

        List<String> rewards = plugin.getConfig().getStringList("rewards.levels." + level + ".display");
        if (rewards.isEmpty()) {
            rewards = List.of(gui.getString("no-rewards", "&7None"));
        }

        ItemStack item = buildItem(player, section, placeholders, rewards);
        if (gui.getBoolean("level-as-amount", false)) {
            item.setAmount(Math.clamp(level, 1, 64));
        }
        return item;
    }

    private void placeButton(Player player, LevelsMenu holder, Inventory inventory, String key,
                             LevelsMenu.Action action, Map<String, String> placeholders) {
        ConfigurationSection section = gui.getConfigurationSection("buttons." + key);
        if (section == null) return;
        int slot = section.getInt("slot", -1);
        if (slot < 0 || slot >= size) return;

        inventory.setItem(slot, buildItem(player, section, placeholders, List.of()));
        if (action != null) holder.setAction(slot, action);
    }

    private ItemStack buildItem(Player player, ConfigurationSection section, Map<String, String> placeholders,
                                List<String> rewards) {
        String materialName = section.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null || !material.isItem()) {
            plugin.getLogger().warning("gui.yml: invalid material '" + materialName + "' in " + section.getCurrentPath());
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (section.contains("name")) {
            meta.displayName(text(player, section.getString("name", ""), placeholders));
        }

        List<Component> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            if (line.contains("%rewards%")) {
                for (String reward : rewards) {
                    lore.add(text(player, line.replace("%rewards%", reward), placeholders));
                }
            } else {
                lore.add(text(player, line, placeholders));
            }
        }
        if (!lore.isEmpty()) meta.lore(lore);

        if (section.getBoolean("glow", false)) {
            meta.setEnchantmentGlintOverride(true);
        }
        if (meta instanceof SkullMeta skull) {
            skull.setOwningPlayer(player);
        }

        item.setItemMeta(meta);
        return item;
    }

    private Map<String, String> basePlaceholders(Player player, XPManager xp, int page, int pages) {
        Map<String, String> map = new HashMap<>();
        map.put("%player%", player.getName());
        map.put("%current_level%", String.valueOf(xp.getLevel(player)));
        map.put("%current_xp%", String.valueOf(xp.getXP(player)));
        map.put("%next_level_xp%", String.valueOf(xp.getNextLevelXP(player)));
        map.put("%remaining_xp%", String.valueOf(Math.max(0, xp.getRemainingXP(player))));
        map.put("%percent%", String.valueOf((int) xp.getLevelPercent(player)));
        map.put("%bar%", xp.getXPBar(player));
        map.put("%page%", String.valueOf(page + 1));
        map.put("%pages%", String.valueOf(pages));
        return map;
    }

    private Component text(Player player, String raw, Map<String, String> placeholders) {
        String result = raw;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }

        Component component = result.contains("<") ? miniMessage.deserialize(result) : legacy.deserialize(result);
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private int firstLevel() {
        return Math.max(1, gui.getInt("first-level", 1));
    }

    private int levelCount() {
        return Math.max(1, gui.getInt("max-level", 100) - firstLevel() + 1);
    }

    private static void copyInto(ConfigurationSection from, ConfigurationSection to) {
        if (from == null) return;
        for (String key : from.getKeys(false)) {
            to.set(key, from.get(key));
        }
    }

    /** Parses entries like "10" or "10-16" into a list of unique slots within the inventory. */
    private List<Integer> parseSlots(List<String> entries, int size) {
        Set<Integer> slots = new LinkedHashSet<>();
        for (String entry : entries) {
            try {
                String[] parts = entry.trim().split("-");
                int start = Integer.parseInt(parts[0].trim());
                int end = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : start;
                for (int slot = Math.min(start, end); slot <= Math.max(start, end); slot++) {
                    if (slot >= 0 && slot < size) slots.add(slot);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("gui.yml: invalid slot '" + entry + "' in level-slots");
            }
        }
        return new ArrayList<>(slots);
    }
}
