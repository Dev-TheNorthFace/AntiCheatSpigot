package North.AntiCheat.Events.Other.AutoArmor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AutoArmor implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private static final long TIME_LIMIT_MS = 1000;
    private final Map<Player, Long> lastEquipTime = new HashMap<>();
    private final Map<Player, Integer> armorCount = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        long currentTime = System.currentTimeMillis();
        if (lastEquipTime.containsKey(player)) {
            long lastTime = lastEquipTime.get(player);
            if ((currentTime - lastTime) < TIME_LIMIT_MS) {
                int count = armorCount.getOrDefault(player, 0) + 1;
                armorCount.put(player, count);
                if (count >= 4) {
                    player.kickPlayer("Vous avez été expulsé pour avoir équipé trop rapidement des pièces d'armure.");
                    sendDiscordNotification(player.getName());
                    armorCount.remove(player);
                    lastEquipTime.remove(player);
                    return;
                }
            }
        }

        lastEquipTime.put(player, currentTime);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastEquipTime.remove(player);
        armorCount.remove(player);
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir équipé trop rapidement des pièces d'armure.\"}";
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