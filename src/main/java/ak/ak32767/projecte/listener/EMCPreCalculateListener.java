package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.event.EMCPreCalculateEvent;
import ak.ak32767.projecte.manager.TransmutationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EMCPreCalculateListener implements Listener {
    private final TransmutationManager.WorldTransmutation worldTransmutation;
    private final TransmutationManager.PhiloCraftTransmutation philoCraftTransmutation;

    public EMCPreCalculateListener(TransmutationManager transmutationManager) {
         this.worldTransmutation = transmutationManager.getWorldTransmutation();
         this.philoCraftTransmutation = transmutationManager.getPhiloCraftTransmutation();
    }

    @EventHandler
    public void onEMCPreCalculateEvent(EMCPreCalculateEvent event) {
        if (this.worldTransmutation.build())
            this.worldTransmutation.conversionsRegister(event.getBuilder());

        if (this.philoCraftTransmutation.build()) {
            this.philoCraftTransmutation.conversionsRegister(event.getBuilder());
            this.philoCraftTransmutation.registerPhilotransRecipes();
            this.philoCraftTransmutation.registerPhilosmeltRecipes();
        }
    }
}
