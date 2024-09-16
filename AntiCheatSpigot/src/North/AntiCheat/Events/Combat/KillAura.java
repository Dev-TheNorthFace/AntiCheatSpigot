package North.AntiCheat.Events.Combat.KillAura;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class KillAura implements Listener {

    private final Map<Player, Long> attackTimes = new HashMap<>();
    private final String discordWebhookUrl = "https://discord.com/api/webhooks/your-webhook-url";

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        long currentTime = System.currentTimeMillis();
        if (!attackTimes.containsKey(player)) {
            attackTimes.put(player, currentTime);
            return;
        }

        long lastAttackTime = attackTimes.get(player);
        long timeDiff = currentTime - lastAttackTime;
        if (timeDiff < 1000) {
            int hitCount = (int) event.getEntity().getNearbyEntities(5, 5, 5).stream()
                .filter(entity -> entity instanceof Player)
                .count();
            if (hitCount > 3) {
                kickPlayer(player);
            }
        }

        attackTimes.put(player, currentTime);
    }

    private void kickPlayer(Player player) {
        Bukkit.getScheduler().runTask(this, () -> {
            player.kickPlayer(ChatColor.RED + "Vous avez été kické pour utilisation de KillAura.");
            Bukkit.getLogger().info(player.getName() + " a été kické pour utilisation de KillAura.");
            sendDiscordMessage(player.getName() + " a été kické pour utilisation de KillAura.");
        });
    }

    private void sendDiscordMessage(String message) {
        new Thread(() -> {
            try {
                URL url = new URL(discordWebhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                String payload = "{\"content\": \"" + message + "\"}";
                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"))) {
                    out.print(payload);
                    out.flush();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Bukkit.getLogger().warning("Failed to send message to Discord. Response code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}