package North.AntiCheat.Events.Mouvements.TpTap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class TpTap implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<Player, Boolean> playerHitAirOrBlock = new HashMap<>();
    private final Map<Player, Boolean> playerHasEnderPearl = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Entity target = event.getEntity();
            if (target instanceof Player) {
                Player targetPlayer = (Player) target;
                if (targetPlayer.getLocation().getY() > damager.getLocation().getY() || damager.getLocation().getBlock().getType().isSolid()) {
                    playerHitAirOrBlock.put(damager, true);
                    playerHasEnderPearl.put(damager, damager.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL);
                } else {
                    playerHitAirOrBlock.put(damager, false);
                    playerHasEnderPearl.put(damager, false);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (playerHitAirOrBlock.getOrDefault(player, false) && playerHasEnderPearl.getOrDefault(player, false) == false) {
            player.kickPlayer("Vous avez été expulsé pour avoir frappé un joueur dans les airs ou sur un bloc sans utiliser une Ender Pearl.");
            sendDiscordNotification(player.getName());
        }

        playerHitAirOrBlock.put(player, false);
        playerHasEnderPearl.put(player, false);
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir frappé un joueur dans les airs ou sur un bloc sans utiliser d'Ender Pearl.\"}";
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