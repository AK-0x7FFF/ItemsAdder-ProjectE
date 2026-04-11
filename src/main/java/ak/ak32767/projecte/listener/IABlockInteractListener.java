package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.ProjectE;
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IABlockInteractListener implements Listener {
    private final ProjectE plugin;

    public IABlockInteractListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockInteract(CustomBlockInteractEvent event) {
        String namespacedId = event.getNamespacedID();
        if (!namespacedId.startsWith("projecte:")) return;

        String blockId = namespacedId.substring(namespacedId.indexOf(":") + 1);

        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        Location location = block.getLocation();

        player.sendMessage("onBlockInteract: " + blockId);
        switch (blockId) {
        }
    }

//    @EventHandler
//    public void onFurnitureInteract(FurnitureInteractEvent event) {
//        String namespacedId = event.getNamespacedID();
//        Player player = event.getPlayer();
//
//        player.sendMessage("onFurnitureInteract: " + namespacedId);
//    }
}