package North.AutoClick.Events.Combat.AutoClick;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AutoClick implements Listener {

    private final Map<UUID, Integer> cpsCount = new HashMap<>();
    private final Map<UUID, Long> lastClickTime = new HashMap<>();
    private static final int MAX_CPS = 20;
    private static final int WARNING_CPS = 15;
    private static final String DISCORD_WEBHOOK_URL = "YOUR_DISCORD_WEBHOOK_URL";

    public CPSListener() {
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("AntiCheatSpîgot"));
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (!lastClickTime.containsKey(playerId)) {
            lastClickTime.put(playerId, currentTime);
            cpsCount.put(playerId, 0);
        }

        long lastTime = lastClickTime.get(playerId);
        if (currentTime - lastTime <= 1000) {
            cpsCount.put(playerId, cpsCount.get(playerId) + 1);
        } else {
            cpsCount.put(playerId, 1);
            lastClickTime.put(playerId, currentTime);
        }

        int cps = cpsCount.get(playerId);
        if (cps > MAX_CPS) {
            player.kickPlayer("AutoClick détecté");
            sendDiscordMessage("Le joueur " + player.getName() + " a été kické pour AutoClick avec " + cps + " CPS.");
        } 
        else if (cps > WARNING_CPS) {
            Bukkit.broadcastMessage("Le joueur " + player.getName() + " fait " + cps + " CPS");
            sendDiscordMessage("Le joueur " + player.getName() + " fait " + cps + " CPS.");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                cpsCount.put(playerId, 0);
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("AntiCheatSpîgot"), 20);
    }

    private void sendDiscordMessage(String message) {
        try {
            URL url = new URL(DISCORD_WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonMessage = "{\"content\": \"" + message + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonMessage.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            connection.getResponseCode(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}