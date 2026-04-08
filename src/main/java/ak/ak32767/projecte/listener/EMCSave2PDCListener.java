package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.emcsys.EMCManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;

public class EMCSave2PDCListener implements Listener {
    private final EMCManager emcManager;

    public EMCSave2PDCListener(EMCManager emcManager) {
        this.emcManager = emcManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.emcManager.savePlayerEMCMap2PDC(player);
        this.emcManager.unloadPlayerOnEMCMap(player);
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        this.emcManager.saveAllPlayerEMCMap2PDC();
    }
}
