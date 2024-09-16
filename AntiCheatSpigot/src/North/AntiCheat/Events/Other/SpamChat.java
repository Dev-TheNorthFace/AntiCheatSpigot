package North.AntiCheat.Events.Other.SpamChat;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SpamChat implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<String, Integer> playerMessages = new HashMap<>();
    private static final int REPEAT_THRESHOLD = 3;
    private static final Pattern IP_PATTERN = Pattern.compile(
        "\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b|\\b(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\b"
    );

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String playerName = event.getPlayer().getName();
        if (IP_PATTERN.matcher(message).find()) {
            event.setCancelled(true);
            sendDiscordNotification(playerName, "Tentative de partage d'adresse IP détectée : " + message);
            return;
        }

        playerMessages.putIfAbsent(playerName, 0);
        int messageCount = playerMessages.get(playerName) + 1;
        playerMessages.put(playerName, messageCount);
        if (messageCount > REPEAT_THRESHOLD && message.equals(event.getMessage())) {
            event.setCancelled(true);
            sendDiscordNotification(playerName, "Le joueur " + playerName + " a répété le même message : " + message);
        } else if (!message.equals(event.getMessage())) {
            playerMessages.put(playerName, 0);
        }
    }

    private void sendDiscordNotification(String playerName, String messageContent) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = String.format(
                    "{\"content\": \"[%s] %s\"}",
                    playerName,
                    messageContent
                );

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                connection.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}