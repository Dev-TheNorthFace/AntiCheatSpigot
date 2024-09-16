package North.AntiCheat.Events.Other.SpamArrow;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpamArrow implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<UUID, Long> lastArrowShoot = new HashMap<>();
    private final long ARROW_SHOOT_THRESHOLD = 1000L;

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            long currentTime = System.currentTimeMillis();
            UUID playerId = player.getUniqueId();
            if (lastArrowShoot.containsKey(playerId)) {
                long lastShootTime = lastArrowShoot.get(playerId);
                if (currentTime - lastShootTime < ARROW_SHOOT_THRESHOLD) {
                    player.kickPlayer("Vous avez été kické pour avoir lancé trop de flèches trop rapidement.");
                    sendDiscordNotification(player.getName());
                    lastArrowShoot.remove(playerId);
                } else {
                    lastArrowShoot.put(playerId, currentTime);
                }
            } else {
                lastArrowShoot.put(playerId, currentTime);
            }
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
                    "{\"content\": \"Le joueur %s a été kické pour avoir lancé trop de flèches trop rapidement.\"}",
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