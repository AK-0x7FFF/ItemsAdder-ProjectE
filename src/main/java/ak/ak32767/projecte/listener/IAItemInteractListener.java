package ak.ak32767.projecte.listener;

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
        if (!event.getAction().isRightClick())
            return;

        ItemStack item = event.getItem();
        if (item == null)
            return;

        CustomStack iaStack = CustomStack.byItemStack(item);
        if (iaStack == null)
            return;

        String id = iaStack.getNamespacedID();
        if (id.equals("projecte:transmutation_tablet")) {
            Player player = event.getPlayer();
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.4f, .85f);
            new TransTableGUI(this.plugin, event.getPlayer());
        }
    }
}
