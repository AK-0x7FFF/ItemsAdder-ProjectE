package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.manager.EMCManager;
import ak.ak32767.projecte.manager.KnowledgeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;

public class DataSave2PDCListener implements Listener {
    private final EMCManager emcManager;
    private final KnowledgeManager knowledgeManager;

    public DataSave2PDCListener(EMCManager emcManager, KnowledgeManager knowledgeManager) {
        this.emcManager = emcManager;
        this.knowledgeManager = knowledgeManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        this.emcManager.savePlayerEMCMap2PDC(player);
        this.emcManager.unloadPlayerOnEMCMap(player);

        this.knowledgeManager.savePlayerKnowledgeMap2PDC(player);
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        this.emcManager.saveAllPlayerEMCMap2PDC();
        this.knowledgeManager.saveAllPlayerKnowledgeMap2PDC();
    }
}
