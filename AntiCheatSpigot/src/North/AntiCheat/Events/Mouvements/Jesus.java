package North.AntiCheat.Events.Mouvements.Jesus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class Jesus implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Material blockBelow = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        Material blockAtFeet = player.getLocation().getBlock().getType();
        if (blockBelow == Material.WATER && blockAtFeet != Material.WATER && !player.isSwimming()) {
            player.kickPlayer("Vous avez été expulsé pour avoir marché sur l'eau.");
            sendDiscordNotification(player.getName());
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
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir marché sur l'eau sans nager.\"}";
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
