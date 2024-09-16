package North.AntiCheat.Events.Mouvements.SpeedHack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class SpeedHack implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private static final double MAX_SPEED = 0.2;
    private final Set<Player> speedHackers = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            return;
        }

        double speed = player.getVelocity().length();
        if (speed > MAX_SPEED) {
            if (!speedHackers.contains(player)) {
                speedHackers.add(player);
                player.kickPlayer("Vous avez été expulsé pour avoir couru trop vite sans effet de potion de vitesse.");
                sendDiscordNotification(player.getName());
            }
        } else {
            speedHackers.remove(player);
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
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir couru trop vite sans effet de potion de vitesse.\"}";
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