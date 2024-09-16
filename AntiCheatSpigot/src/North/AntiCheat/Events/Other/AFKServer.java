package North.AntiCheat.Events.Other.AFKServer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AFKServer implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private static final long INACTIVITY_THRESHOLD = 10 * 60 * 1000;
    private final Map<Player, Long> lastActivityMap = new HashMap<>();
    private final CyclicBarrier barrier = new CyclicBarrier(1);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        startActivityCheckTask();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        lastActivityMap.put(player, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastActivityMap.remove(player);
    }

    private void startActivityCheckTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<Player, Long> entry : lastActivityMap.entrySet()) {
                Player player = entry.getKey();
                long lastActivityTime = entry.getValue();
                if (currentTime - lastActivityTime > INACTIVITY_THRESHOLD) {
                    player.kickPlayer("Vous avez été expulsé pour inactivité.");
                    sendDiscordNotification(player.getName());
                }
            }
        }, 0L, 20L * 60L);
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour inactivité de plus de 10 minutes.\"}";
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