package ak.ak32767.projecte.listener.itemsadder;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.gui.TransTableGUI;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class IAItemInteractListener implements Listener {
    private final ProjectE plugin;

    public IAItemInteractListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null)
            return;

        CustomStack iaItem = CustomStack.byItemStack(item);
        if (iaItem == null)
            return;

        if (event.getAction().isLeftClick()) {
            this.LeftClickInteract(iaItem, event);
            return;
        }

        if (event.getAction().isRightClick()) {
            this.RightClickInteract(iaItem, event);
            return;
        }
    }

    private void LeftClickInteract(CustomStack iaItem, PlayerInteractEvent event) {

    }

    private void RightClickInteract(CustomStack iaItem, PlayerInteractEvent event) {
        String id = iaItem.getNamespacedID();

        switch (id) {
            case "projecte:transmutation_tablet" -> {
                Player player = event.getPlayer();
                player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.4f, .85f);

                TransTableGUI gui = new TransTableGUI(this.plugin, player);
                gui.openInventory();
            }
        }
    }
}
