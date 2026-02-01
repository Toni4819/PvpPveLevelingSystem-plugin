package org.toni_4819.pvpPveLevelingSystem.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.toni_4819.pvpPveLevelingSystem.PvpPveLevelingSystem;

import java.io.File;
import java.util.Map;

public class LangManager {
    private final PvpPveLevelingSystem plugin;
    private FileConfiguration langConfig;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    public LangManager(PvpPveLevelingSystem plugin) {
        this.plugin = plugin;
        loadLang();
    }

    public void loadLang() {
        String lang = plugin.getConfig().getString("lang", "en_us");
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");

        if (!langFile.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public Component getMessage(String key) {
        return getMessage(key, null);
    }

    public Component getMessage(String key, Map<String, String> placeholders) {
        String prefix = langConfig.getString("prefix", "&8[&6Level&8] ");
        String msg = langConfig.getString("messages." + key, key);

        String full = prefix + msg;

        // Remplacer les placeholders (%command%, %player%, %level%, etc.)
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                full = full.replace(entry.getKey(), entry.getValue());
            }
        }

        // MiniMessage si tags présents
        if (full.contains("<")) {
            return miniMessage.deserialize(full);
        }

        // Sinon legacy (&)
        return legacy.deserialize(full);
    }
}
