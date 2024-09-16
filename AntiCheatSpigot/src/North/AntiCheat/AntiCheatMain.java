package North.AnitCheat.AntiCheatSpigot;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiCheatSpigot extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        try {
            Bukkit.getPluginManager().registerEvents(this, this);
            getLogger().info("AntiCheatSpigot : ON");
        } catch (Exception e) {
            getLogger().severe("AntiCheatSpigot : ERROR CODE");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiCheatSpigot : OFF");
    }

    @Override
    public void onLoad() {
        getLogger().info("AntiCheatSpigot : REDEM");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("xBibou te surveille x)");
    }
}