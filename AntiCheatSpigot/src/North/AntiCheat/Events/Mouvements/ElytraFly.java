package North.AntiCheat.Events.Mouvements.ElytraFly;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ElytraFly implements Listener {

    private static final int MAX_FLY_TIME_WITHOUT_FIREWORK = 100;
    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<Player, Integer> flightTimeWithoutFirework = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        startFlightCheckTask();
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();  
        if (player.isGliding()) {
            flightTimeWithoutFirework.put(player, 0);
        } else {
            flightTimeWithoutFirework.remove(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isGliding()) {
            if (player.getInventory().getItemInMainHand().getType().toString().contains("FIREWORK")) {
                flightTimeWithoutFirework.put(player, 0);
            }
        }
    }

    private void startFlightCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isGliding()) {
                        int flyTime = flightTimeWithoutFirework.getOrDefault(player, 0) + 1;
                        if (flyTime > MAX_FLY_TIME_WITHOUT_FIREWORK) {
                            player.kickPlayer("Vous avez été expulsé pour avoir volé sans utiliser de feu d'artifice.");
                            sendDiscordNotification(player.getName());
                            flightTimeWithoutFirework.remove(player);
                        } else {
                            flightTimeWithoutFirework.put(player, flyTime);
                        }
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
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir volé avec des Élytres sans utiliser de feu d'artifice.\"}";
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