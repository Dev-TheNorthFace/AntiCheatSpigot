package North.AntiCheat.Events.Other.MineralsBreakInfo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MineralsBreakInfo implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";
    private Map<Player, Map<Material, Integer>> playerMineralCount = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();
        if (isTrackedMineral(material)) {
            playerMineralCount.putIfAbsent(player, new HashMap<>());
            Map<Material, Integer> mineralCount = playerMineralCount.get(player);
            mineralCount.put(material, mineralCount.getOrDefault(material, 0) + 1);
            Material mostMinedMineral = getMostMinedMineral(mineralCount);
            sendDiscordNotification(player.getName(), material.name(), mostMinedMineral.name(), mineralCount.get(mostMinedMineral));
        }
    }

    private boolean isTrackedMineral(Material material) {
        switch (material) {
            case COAL_ORE:
            case REDSTONE_ORE:
            case DIAMOND_ORE:
            case LAPIS_ORE:
            case IRON_ORE:
            case GOLD_ORE:
            case EMERALD_ORE:
            case QUARTZ_ORE:
                return true;
            default:
                return false;
        }
    }

    private Material getMostMinedMineral(Map<Material, Integer> mineralCount) {
        Material mostMinedMineral = null;
        int maxCount = 0;
        for (Map.Entry<Material, Integer> entry : mineralCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostMinedMineral = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        return mostMinedMineral;
    }

    private void sendDiscordNotification(String playerName, String minedMineral, String mostMinedMineral, int mostMinedCount) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = String.format(
                    "{\"content\": \"Le joueur %s a miné le minerai %s. Voila le minerai qu'il a le plus miné : %d %s\"}",
                    playerName, minedMineral, mostMinedCount, mostMinedMineral
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