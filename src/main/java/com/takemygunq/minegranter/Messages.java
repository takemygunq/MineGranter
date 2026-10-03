package com.takemygunq.minegranter;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/** Chat messages from the "messages" config section, with the prefix and & color codes applied. */
public final class Messages {

    private final MineGranter plugin;
    private final Map<String, String> messages = new HashMap<>();
    private String prefix = "";

    Messages(MineGranter plugin) {
        this.plugin = plugin;
    }

    void load(FileConfiguration config) {
        messages.clear();
        ConfigurationSection section = config.getConfigurationSection("messages");
        if (section == null) return;
        for (String key : section.getKeys(false)) messages.put(key, section.getString(key, ""));
        prefix = messages.getOrDefault("prefix", "");
    }

    /** Message text with placeholders replaced: format("role_given", "player", name, "role", prefix). */
    public String format(String key, String... placeholders) {
        String text = messages.get(key);
        if (text == null) {
            plugin.getLogger().warning("Missing message in config.yml: messages." + key);
            text = key;
        }
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return color(prefix + text);
    }

    public void send(CommandSender to, String key, String... placeholders) {
        to.sendMessage(format(key, placeholders));
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
