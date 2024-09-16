package North.AntiCheat.Events.Mouvements.Bhop;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Bhop implements Listener {

    private static final int MAX_SUSPICIOUS_JUMPS = 5;
    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<Player, Integer> suspiciousJumpCounts = new HashMap<>();
    private final Map<Player, Boolean> hasPressedJump = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        startJumpCheckTask();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isOnGround()) {
            if (!hasPressedJump.getOrDefault(player, false)) {
                int suspiciousJumps = suspiciousJumpCounts.getOrDefault(player, 0) + 1;
                if (suspiciousJumps > MAX_SUSPICIOUS_JUMPS) {
                    player.kickPlayer("Vous avez été expulsé pour avoir sauté sans appuyer sur la touche de saut.");
                    sendDiscordNotification(player.getName());
                    suspiciousJumpCounts.remove(player);
                } else {
                    suspiciousJumpCounts.put(player, suspiciousJumps);
                }
            }
        } else {
            hasPressedJump.put(player, false);
        }
    }

    private void startJumpCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOnGround()) {
                        suspiciousJumpCounts.put(player, 0);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir sauté sans appuyer sur la touche de saut.\"}";
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