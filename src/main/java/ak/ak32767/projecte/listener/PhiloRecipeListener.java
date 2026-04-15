package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.manager.PhiloRecipeManager;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PhiloRecipeListener implements Listener {
    private ProjectE plugin;
    private PhiloRecipeManager manager;

    public PhiloRecipeListener(PhiloRecipeManager manager, ProjectE plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    // 神秘，十分神秘
    // 只有在 priority = EventPriority.MONITOR 的優先級下才能正確的 setResult
    // 私人 IA 整嫩個高優先幹nmb呢
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof Keyed keyed))
            return;

        if (!keyed.getKey().getNamespace().equalsIgnoreCase(PhiloRecipeManager.PHILO_RECIPE_NAMESPACE))
            return;

        boolean havePhiloStone = false;
        ItemStack[] matrixRaw = event.getInventory().getMatrix();
        for (ItemStack item : matrixRaw) {
            if (item == null || !item.getType().equals(Material.PHANTOM_MEMBRANE))
                continue;

            CustomStack iaItem = CustomStack.byItemStack(item);
            if (iaItem != null && iaItem.getNamespacedID().equals(PhiloRecipeManager.PHILOSTONE_NAMESPACEDID)) {
                havePhiloStone = true;
                break;
            }
        }
        if  (!havePhiloStone) {
            event.getInventory().setResult(null);
            return;
        }

        List<ItemStack> matrix = Arrays.stream(matrixRaw)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ObjectArrayList::new));

        event.getInventory().setResult(null);
        ItemStack result = this.manager.transmutationRecipe(matrix);
        if (result == null)
            return;

//        Bukkit.getScheduler().runTask(this.plugin, () -> {
//            event.getInventory().setResult(result);
//            event.getViewers().forEach(human -> {if (human instanceof Player p) p.updateInventory(); });
//        });
        event.getInventory().setResult(result);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof Keyed keyed))
            return;

        if (!keyed.getKey().getNamespace().equalsIgnoreCase(PhiloRecipeManager.PHILO_RECIPE_NAMESPACE))
            return;

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        for (int i = 0; i < matrix.length; ++i) {
            ItemStack item = matrix[i];
            if (item == null || item.isEmpty())
                continue;

            CustomStack iaItem = CustomStack.byItemStack(item);
            if (iaItem == null || !iaItem.getNamespacedID().equals(PhiloRecipeManager.PHILOSTONE_NAMESPACEDID))
                continue;

            int slot = i + 1;
            ItemStack tItem = item.clone();
            plugin.logger.info(slot + " " + tItem);

            Bukkit.getScheduler().runTask(this.plugin, () -> inventory.setItem(slot, tItem));
            break;
        }
    }
}
