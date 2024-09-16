package North.AntiCheat.Events.Mouvements.GhostHand;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class GhostHand implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            if (isWallBetween(attacker, victim)) {
                attacker.kickPlayer("Vous avez été expulsé pour avoir frappé à travers un mur.");
                sendDiscordNotification(attacker.getName());
            }
        }
    }

    private boolean isWallBetween(Player attacker, Player victim) {
        BlockIterator blockIterator = new BlockIterator(attacker.getWorld(), attacker.getEyeLocation().toVector(), victim.getEyeLocation().toVector().subtract(attacker.getEyeLocation().toVector()), 0, (int) attacker.getLocation().distance(victim.getLocation()));
        while (blockIterator.hasNext()) {
            Material material = blockIterator.next().getType();
            if (material.isSolid()) {
                return true;
            }
        }
        return false;
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir frappé à travers un mur.\"}";
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