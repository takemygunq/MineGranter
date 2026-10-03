package com.takemygunq.minegranter;

import com.google.gson.JsonObject;
import org.bukkit.configuration.file.FileConfiguration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Sends notifications to Telegram chats through the Bot API.
 * Requests are asynchronous, so a slow or unreachable Telegram never blocks the server thread.
 */
public final class TelegramNotifier {

    public enum Chat { STAFF, REPORTS }

    private static final String API = "https://api.telegram.org/bot";

    private final MineGranter plugin;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, String> templates = new HashMap<>();
    private boolean enabled;
    private String token = "";
    private String staffChat = "";
    private String reportsChat = "";

    TelegramNotifier(MineGranter plugin) {
        this.plugin = plugin;
    }

    void load(FileConfiguration config) {
        token = config.getString("telegram.bot_token", "").trim();
        staffChat = config.getString("telegram.staff_chat_id", "").trim();
        reportsChat = config.getString("telegram.reports_chat_id", "").trim();
        enabled = config.getBoolean("telegram.enabled") && !token.isEmpty();
        if (config.getBoolean("telegram.enabled") && token.isEmpty()) {
            plugin.getLogger().warning("Telegram is enabled but telegram.bot_token is empty — notifications are off");
        }
        templates.clear();
        var section = config.getConfigurationSection("telegram.messages");
        if (section != null) {
            for (String key : section.getKeys(false)) templates.put(key, section.getString(key, ""));
        }
    }

    /** Fills the template telegram.messages.<key> and sends it to the chat. Does nothing if Telegram is off. */
    public void notify(Chat chat, String key, String... placeholders) {
        String chatId = chat == Chat.STAFF ? staffChat : reportsChat;
        String template = templates.get(key);
        if (!enabled || chatId.isEmpty() || template == null || template.isEmpty()) return;

        String text = template;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        JsonObject body = new JsonObject();
        body.addProperty("chat_id", chatId);
        body.addProperty("text", text);
        body.addProperty("disable_web_page_preview", true);

        HttpRequest request = HttpRequest.newBuilder(URI.create(API + token + "/sendMessage"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        http.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, error) -> {
            // The URL contains the bot token, so only the status and Telegram's description are logged
            if (error != null) {
                plugin.getLogger().warning("Telegram notification failed: " + error.getClass().getSimpleName());
            } else if (response.statusCode() != 200) {
                plugin.getLogger().warning("Telegram returned " + response.statusCode() + ": " + response.body());
            }
        });
    }
}
