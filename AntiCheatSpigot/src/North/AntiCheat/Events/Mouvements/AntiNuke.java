package North.AntiCheat.Events.Mouvements.AntiNuke;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AntiNuke implements Listener {

    private static final int MAX_BLOCKS = 10;
    private static final long TIME_FRAME = 1000;
    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<Player, Long> blockBreakTimes = new HashMap<>();
    private final Map<Player, Integer> blockBreakCounts = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.HAMMER)) {
            long currentTime = System.currentTimeMillis();
            blockBreakTimes.putIfAbsent(player, currentTime);
            blockBreakCounts.putIfAbsent(player, 0);
            long lastBreakTime = blockBreakTimes.get(player);
            int currentCount = blockBreakCounts.get(player);
            if (currentTime - lastBreakTime < TIME_FRAME) {
                blockBreakCounts.put(player, currentCount + 1);
            } else {
                blockBreakCounts.put(player, 1);
            }

            blockBreakTimes.put(player, currentTime);
            if (blockBreakCounts.get(player) > MAX_BLOCKS) {
                player.kickPlayer("Vous avez été expulsé pour avoir cassé trop de blocs trop rapidement");
                sendDiscordNotification(player.getName());
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
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir cassé trop de blocs en peu de temps.\"}";
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