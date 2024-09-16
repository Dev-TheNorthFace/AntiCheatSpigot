package North.AutoClick.Events.Combat.HitBox;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HitBox implements Listener {

    private static final double DISTANCE_THRESHOLD = 3.5;
    private static final double HEIGHT_THRESHOLD = 1.0;
    private static final int ACTION_THRESHOLD = 1;
    private static final String DISCORD_WEBHOOK_URL = "https://your-webhook-url-here";
    private final Map<String, Integer> hitCount = new HashMap<>();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (damager instanceof Player && entity instanceof Player) {
            Player joueurAttaquant = (Player) damager;
            Player joueurCible = (Player) entity;
            if (isHittingTooFar(joueurAttaquant, joueurCible) || isHittingTooHigh(joueurAttaquant, joueurCible)) {
                incrementHitCount(joueurAttaquant);
            }
        }
    }

    private boolean isHittingTooFar(Player attaquant, Player cible) {
        double distance = attaquant.getLocation().distance(cible.getLocation());
        return distance > DISTANCE_THRESHOLD;
    }

    private boolean isHittingTooHigh(Player attaquant, Player cible) {
        double differenceHauteur = cible.getLocation().getY() - attaquant.getLocation().getY();
        return differenceHauteur > HEIGHT_THRESHOLD;
    }

    private void incrementHitCount(Player joueur) {
        String nomJoueur = joueur.getName();
        hitCount.put(nomJoueur, hitCount.getOrDefault(nomJoueur, 0) + 1);

        if (hitCount.get(nomJoueur) >= ACTION_THRESHOLD) {
            kickPlayerForDistanceHack(joueur);
        }
    }

    private void kickPlayerForDistanceHack(Player joueur) {
        joueur.kickPlayer("AntiHitBox : Vous avez été expulsé pour avoir frappé trop loin ou trop haut");
        getLogger().info("Le joueur " + joueur.getName() + " a été expulsé par AntiHitBox.");
        hitCount.remove(joueur.getName());

        sendWebhookNotification(joueur.getName());
    }

    private void sendWebhookNotification(String playerName) {
        try {
            URL url = new URL(DISCORD_WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            String jsonPayload = "{\"content\": \"Le joueur " + playerName + " a été expulsé pour hitbox !\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            connection.getResponseCode();
        } catch (Exception e) {
            getLogger().severe("Erreur lors de l'envoi du webhook Discord : " + e.getMessage());
        }
    }
}