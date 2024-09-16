package North.AntiCheat.Events.Other.InstaBreak;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class InstaBreak implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        if (blockType == Material.OBSIDIAN) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName() &&
                itemInHand.getItemMeta().getDisplayName().equals(ChatColor.RESET + "Casseur à obsidienne")) {
                return;
            }

            if (event.getBlock().getDrops().size() > 0 && event.getExpToDrop() == 0) {
                player.kickPlayer("Vous avez été expulsé pour avoir cassé de l'obsidienne en un coup sans 'Casseur à obsidienne'.");
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
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir cassé de l'obsidienne en un coup sans 'Casseur à obsidienne'.\"}";
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