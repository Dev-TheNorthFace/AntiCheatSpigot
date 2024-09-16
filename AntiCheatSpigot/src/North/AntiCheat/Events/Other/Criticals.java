package North.AntiCheat.Events.Other.Criticals;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class Criticals implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isCriticalHit(player)) {
                if (!player.isFlying() && !isJumping(player)) {
                    player.kickPlayer("Vous avez été expulsé pour avoir effectué un coup critique sans sauter.");
                    sendDiscordNotification(player.getName());
                }
            }
        }
    }

    private boolean isCriticalHit(Player player) {
        return player.getFallDistance() > 0.0f && !player.isOnGround() && !player.isInWater() && !player.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS);
    }

    private boolean isJumping(Player player) {
        Vector velocity = player.getVelocity();
        return velocity.getY() > 0.0;
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir effectué un coup critique sans sauter.\"}";
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