package North.AntiCheat.Events.Other.RemoveAutoEffets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RemoveAutoEffets implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<UUID, Long> lastEffectRemove = new HashMap<>();
    private final long EFFECT_REMOVE_THRESHOLD = 1000L;

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        if (command.startsWith("/effect")) {
            return;
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    }

    private void checkAndKick(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (lastEffectRemove.containsKey(playerId)) {
            long lastRemoveTime = lastEffectRemove.get(playerId);
            if (currentTime - lastRemoveTime < EFFECT_REMOVE_THRESHOLD) {
                player.kickPlayer("Vous avez été kick pour avoir retiré tous vos effets trop rapidement.");
                sendDiscordNotification(player.getName());
                lastEffectRemove.remove(playerId);
            }
        } else {
            lastEffectRemove.put(playerId, currentTime);
        }
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = String.format(
                    "{\"content\": \"Le joueur %s a été kick pour avoir retiré tous ses effets trop rapidement.\"}",
                    playerName
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