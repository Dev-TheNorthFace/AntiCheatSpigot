package North.AntiCheat.Events.Combat.NoKnockBack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NoKnockBack implements Listener {

    private final Map<Player, Double> lastHealth = new HashMap<>();
    private final String discordWebhookUrl = "https://discord.com/api/webhooks/your-webhook-url";

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (!(event.getEntity() instanceof Player)) return;
        Player target = (Player) event.getEntity();
        lastHealth.put(target, target.getHealth());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (lastHealth.containsKey(player)) {
            double lastHp = lastHealth.get(player);
            double currentHp = player.getHealth();
            if (currentHp >= lastHp) {
                kickPlayer(player);
            }
            lastHealth.remove(player);
        }
    }

    private void kickPlayer(Player player) {
        Bukkit.getScheduler().runTask(this, () -> {
            player.kickPlayer(ChatColor.RED + "Vous avez été kické pour utilisation de Knockback.");
            Bukkit.getLogger().info(player.getName() + " a été kické pour utilisation de Knockback.");
            sendDiscordMessage(player.getName() + " a été kické pour utilisation de Knockback.");
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