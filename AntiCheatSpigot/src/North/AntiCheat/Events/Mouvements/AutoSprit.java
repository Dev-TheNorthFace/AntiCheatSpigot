package North.AntiCheat.Events.Mouvements.AutoSprit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AutoSprit implements Listener {

    private static final long MAX_SPRINT_DURATION = 500;
    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<Player, Long> sprintStartTimes = new HashMap<>();
    private final Map<Player, Boolean> isSprinting = new HashMap<>();

    @EventHandler
    public void onPlayerSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        boolean sprinting = event.isSprinting();
        long currentTime = System.currentTimeMillis();
        if (sprinting) {
            sprintStartTimes.put(player, currentTime);
            isSprinting.put(player, true);
        } else {
            isSprinting.put(player, false);
        }
        if (isSprinting.getOrDefault(player, false)) {
            long sprintStartTime = sprintStartTimes.getOrDefault(player, currentTime);
            if (currentTime - sprintStartTime > MAX_SPRINT_DURATION) {
                player.kickPlayer("Vous avez été expulsé pour avoir couru sans maintenir la touche");
                sendDiscordNotification(player.getName());
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
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir couru sans maintenir la touche.\"}";
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