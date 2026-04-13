package ak.ak32767.projecte.gui;

import ak.ak32767.projecte.ProjectE;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

public abstract class GUIBase {
    final ProjectE plugin;
    final Player player;
    static final Map<UUID, GUIBase> instances = new Object2ObjectOpenHashMap<>();

    public GUIBase(ProjectE plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        this.setupGUI(plugin, player);
        instances.put(player.getUniqueId(), this);
    }

    public static GUIBase getInstance(Player player) {
        return getInstance(player.getUniqueId());
    }

    public static GUIBase getInstance(UUID uuid) {
        return instances.get(uuid);
    }

    public abstract Inventory getInventory();

    public abstract void openInventory();

    protected abstract void setupGUI( ProjectE plugin, Player player);

    protected boolean isStorageSlot(int slot) {
        return slot < this.getInventory().getSize();
    }

    public void onInventoryClick(InventoryClickEvent event) {
        if (this.isStorageSlot(event.getRawSlot()))
            onStorageClick(event);
        else
            onPlayerInventoryClick(event);
    }
    protected abstract void onStorageClick(InventoryClickEvent event);
    protected abstract void onPlayerInventoryClick(InventoryClickEvent event);

    public abstract void onInventoryDrag(InventoryDragEvent event);

    public void onInventoryClose(InventoryCloseEvent event) {
        instances.remove(event.getPlayer().getUniqueId());
        this.onInventoryClosePost(event);
    };
    public abstract void onInventoryClosePost(InventoryCloseEvent event);
}
