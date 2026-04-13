package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.ProjectEException;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.FileNotFoundException;

public class IALoadedListener implements Listener {
    private final ProjectE plugin;

    public IALoadedListener(ProjectE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemsAdderLoaded(ItemsAdderLoadDataEvent event) throws FileNotFoundException, ProjectEException.YAMLKeyOrValueErrorException {
        this.plugin.getEmcManager().build();
    }
}
