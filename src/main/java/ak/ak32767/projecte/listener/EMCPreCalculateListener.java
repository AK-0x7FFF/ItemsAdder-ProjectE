package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.event.EMCPreCalculateEvent;
import ak.ak32767.projecte.manager.TransmutationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EMCPreCalculateListener implements Listener {
    private final TransmutationManager.WorldTransmutation worldTransmutation;

    public EMCPreCalculateListener(TransmutationManager transmutationManager) {
         this.worldTransmutation = transmutationManager.getWorldTransmutation();
    }

    @EventHandler
    public void onEMCPreCalculateEvent(EMCPreCalculateEvent event) {
        if (this.worldTransmutation.build())
            this.worldTransmutation.conversionsRegister(event.getBuilder());
    }
}
