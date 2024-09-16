package North.AntiCheat.Events.Other.FastLadderStairs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

public class FastLadderStairs implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private HashMap<UUID, Long> lastClimbTime = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (isClimbing(player)) {
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            if (lastClimbTime.containsKey(playerId)) {
                long lastTime = lastClimbTime.get(playerId);
                if (currentTime - lastTime < 500) {
                    player.kickPlayer("Vous avez été expulsé pour avoir grimpé trop rapidement.");
                    sendDiscordNotification(player.getName());
                }
            }
            lastClimbTime.put(playerId, currentTime);
        }
    }

    private boolean isClimbing(Player player) {
        Location loc = player.getLocation();
        Material blockType = loc.getBlock().getType();
        return blockType == Material.LADDER || blockType == Material.VINE || blockType == Material.SCAFFOLDING || blockType == Material.STONE_STAIRS || blockType == Material.WOODEN_STAIRS;
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir grimpé trop rapidement.\"}";
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