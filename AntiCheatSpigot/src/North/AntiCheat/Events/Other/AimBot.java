package North.AntiCheat.Events.Other.AimBot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AimBot implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private static final long TARGET_SWITCH_TIME_LIMIT = 200;
    private final Map<UUID, Long> lastTargetChange = new HashMap<>();
    private final Map<UUID, Entity> lastTarget = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity target = event.getRightClicked();
        UUID playerId = player.getUniqueId();
        if (lastTarget.containsKey(playerId)) {
            Entity previousTarget = lastTarget.get(playerId);
            long lastChangeTime = lastTargetChange.getOrDefault(playerId, 0L);
            long currentTime = System.currentTimeMillis();
            if (previousTarget != null && !previousTarget.equals(target) && (currentTime - lastChangeTime < TARGET_SWITCH_TIME_LIMIT)) {
                player.kickPlayer("Vous avez été expulsé pour avoir changé de cible trop rapidement.");
                sendDiscordNotification(player.getName());
            }
        }

        lastTarget.put(playerId, target);
        lastTargetChange.put(playerId, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        lastTarget.remove(playerId);
        lastTargetChange.remove(playerId);
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir changé de cible trop rapidement.\"}";
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