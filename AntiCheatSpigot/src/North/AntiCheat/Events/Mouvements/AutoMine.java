package North.AntiCheat.Events.Mouvements.AutoMine;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AutoMine implements Listener {

    private static final long MAX_TIME_WITHOUT_ACTION = 500;
    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<Player, Long> lastActionTimes = new HashMap<>();
    private final Map<Player, Long> blockBreakTimes = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        long currentTime = System.currentTimeMillis();
        blockBreakTimes.putIfAbsent(player, currentTime);
        long lastActionTime = lastActionTimes.getOrDefault(player, currentTime);
        if (currentTime - lastActionTime > MAX_TIME_WITHOUT_ACTION) {
            player.kickPlayer("Vous avez été expulsé pour avoir cassé des blocs sans appuyer sur la touche de casse");
            sendDiscordNotification(player.getName());
        } else {
            lastActionTimes.put(player, currentTime);
        }
        blockBreakTimes.put(player, currentTime);
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir cassé des blocs sans appuyer sur la touche de casse.\"}";
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