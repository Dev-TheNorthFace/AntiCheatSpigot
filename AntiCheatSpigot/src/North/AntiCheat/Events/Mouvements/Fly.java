package North.AntiCheat.Events.Mouvements.Fly;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class Fly implements Listener {

    private static final String WEBHOOK_URL = "VOTRE_URL_WEBHOOK_DISCORD";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        startFlyCheckTask();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isFlying() && !player.getGameMode().equals(GameMode.CREATIVE) && !player.isOp()) {
            player.kickPlayer("Vous avez été expulsé pour avoir volé sans permissions.");
            sendDiscordNotification(player.getName());
        }
    }

    private void startFlyCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isFlying() && !player.getGameMode().equals(GameMode.CREATIVE) && !player.isOp()) {
                        player.kickPlayer("Vous avez été expulsé pour avoir volé sans permissions.");
                        sendDiscordNotification(player.getName());
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void sendDiscordNotification(String playerName) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour avoir volé sans permissions.\"}";
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