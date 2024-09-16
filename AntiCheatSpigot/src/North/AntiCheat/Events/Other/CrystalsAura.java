package North.AntiCheat.Events.Other.CrystalsAura;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class CrystalsAura implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";

    @EventHandler
    public void onEnderCrystalExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof EnderCrystal) {
            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
                if (!(damageEvent.getDamager() instanceof Player)) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.kickPlayer("Vous avez été expulsé pour avoir cassé un cristal de l'Ender sans l'attaquer directement.");
                        sendDiscordNotification(player.getName());
                    }
                }
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
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir détruit un cristal de l'Ender sans coup direct.\"}";
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