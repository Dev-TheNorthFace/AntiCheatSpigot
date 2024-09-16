package North.AntiCheat.Events.Other.InventoryMove;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class InventoryMove implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private Map<Player, Long> lastInventoryChangeTime = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = player.getInventory();
        long currentTime = System.currentTimeMillis();
        if (isHoldingInventoryShuffle(player)) {
            return;
        }

        if (!lastInventoryChangeTime.containsKey(player)) {
            lastInventoryChangeTime.put(player, currentTime);
            return;
        }

        long lastChangeTime = lastInventoryChangeTime.get(player);
        if (currentTime - lastChangeTime < 1000 && isInventoryChangedOneShot(inventory)) {
            player.kickPlayer("Vous avez été expulsé pour avoir déplacé tous les items de votre inventaire en un instant.");
            sendDiscordNotification(player.getName());
        }
        lastInventoryChangeTime.put(player, currentTime);
    }

    private boolean isInventoryChangedOneShot(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        boolean empty = true;
        for (ItemStack item : contents) {
            if (item != null) {
                empty = false;
                break;
            }
        }
        return empty;
    }

    private boolean isHoldingInventoryShuffle(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.hasItemMeta()) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().equals("Inventory Shuffle")) {
                return true;
            }
        }
        return false;
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir déplacé tous les items de son inventaire en un instant.\"}";
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