package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.ProjectE;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class IABlockBreakListener implements Listener {
    private final ProjectE plugin;

    public IABlockBreakListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(CustomBlockBreakEvent event) {
        String namespacedId = event.getNamespacedID();
        if (!namespacedId.startsWith("projecte:")) return;

        String blockId = namespacedId.substring(namespacedId.indexOf(":") + 1);

//        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        switch (blockId) {
        }
    }
}