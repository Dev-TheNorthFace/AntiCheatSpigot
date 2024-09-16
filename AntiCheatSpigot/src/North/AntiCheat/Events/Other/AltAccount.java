package North.AntiCheat.Events.Other.AltAccount;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AltAccount implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private final Map<String, Set<String>> ipToPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ipAddress = player.getAddress().getHostString();
        String playerName = player.getName();
        ipToPlayers.computeIfAbsent(ipAddress, k -> new HashSet<>()).add(playerName);
        if (ipToPlayers.get(ipAddress).size() > 1) {
            for (String name : ipToPlayers.get(ipAddress)) {
                Player p = Bukkit.getPlayer(name);
                if (p != null) {
                    p.kickPlayer("Vous avez été expulsé car plusieurs comptes se sont connectés depuis la même adresse IP.");
                }
            }

            sendDiscordNotification(ipAddress);
            ipToPlayers.remove(ipAddress);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String ipAddress = player.getAddress().getHostString();
        String playerName = player.getName();
        Set<String> players = ipToPlayers.get(ipAddress);
        if (players != null) {
            players.remove(playerName);
            if (players.isEmpty()) {
                ipToPlayers.remove(ipAddress);
            }
        }
    }

    private void sendDiscordNotification(String ipAddress) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Les joueurs se sont connectés depuis la même adresse IP : " + ipAddress + ". Tous les comptes ont été expulsés.\"}";
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