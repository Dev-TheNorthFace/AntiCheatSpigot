package North.AntiCheat.Events.Other.PacketBlock;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

public class PacketBlock implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final int MAX_PACKETS_PER_SECOND = 50;
    private HashMap<UUID, Integer> packetCount = new HashMap<>();
    private HashMap<UUID, Long> lastPacketTime = new HashMap<>();

    @Override
    public void onEnable() {
        startPacketResetTask();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        handlePacket(event.getPlayer());
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        handlePacket(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        handlePacket(event.getPlayer());
    }

    private void handlePacket(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        lastPacketTime.putIfAbsent(playerId, currentTime);
        packetCount.putIfAbsent(playerId, 0);
        long timeDifference = currentTime - lastPacketTime.get(playerId);
        if (timeDifference > 1000) {
            packetCount.put(playerId, 0);
            lastPacketTime.put(playerId, currentTime);
        }

        int currentPacketCount = packetCount.get(playerId) + 1;
        packetCount.put(playerId, currentPacketCount);
        if (currentPacketCount > MAX_PACKETS_PER_SECOND) {
            player.kickPlayer("Vous avez été kick pour envoi excessif de paquets (flood).");
            sendDiscordNotification(player.getName());
            packetCount.put(playerId, 0);
            lastPacketTime.put(playerId, currentTime);
        }
    }

    private void startPacketResetTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> packetCount.clear(), 20L, 20L);
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = String.format(
                    "{\"content\": \"Le joueur %s a été kick pour envoi excessif de paquets.\"}",
                    playerName
                );

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