package North.AntiCheat.Events.Mouvements.AutoSneak;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AutoSneak implements Listener {

    private static final long MAX_SNEAK_TIME = 500;
    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<Player, Long> sneakStartTimes = new HashMap<>();
    private final Map<Player, Boolean> isSneaking = new HashMap<>();

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (event.isSneaking()) {
            long currentTime = System.currentTimeMillis();
            sneakStartTimes.put(player, currentTime);
            isSneaking.put(player, true);
        } else {
            isSneaking.put(player, false);
        }
        if (isSneaking.getOrDefault(player, false)) {
            long sneakStartTime = sneakStartTimes.getOrDefault(player, System.currentTimeMillis());
            long currentTime = System.currentTimeMillis();
            if (currentTime - sneakStartTime > MAX_SNEAK_TIME) {
                player.kickPlayer("Vous avez été expulsé pour être resté en sneak trop longtemps sans appuyer sur la touche");
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
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour être resté en sneak trop longtemps sans appuyer sur la touche.\"}";
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