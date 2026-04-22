package ak.ak32767.projecte.listener;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.manager.PermissionManager;
import ak.ak32767.projecte.manager.TransmutationManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
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

import static ak.ak32767.projecte.data.PhilosmeltRecipe.PHILOSMELT_RECIPE_NAMESPACE;
import static ak.ak32767.projecte.data.PhilotransRecipe.PHILOTRANS_RECIPE_NAMESPACE;
import static ak.ak32767.projecte.manager.TransmutationManager.PhiloCraftTransmutation.isPhilostoneItem;

public class PhiloRecipeListener implements Listener {
    private ProjectE plugin;
    private TransmutationManager.PhiloCraftTransmutation manager;

    public PhiloRecipeListener(TransmutationManager.PhiloCraftTransmutation manager, ProjectE plugin) {
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

        String namespace = keyed.getKey().getNamespace();
        if (!namespace.equalsIgnoreCase(PHILOTRANS_RECIPE_NAMESPACE) && !namespace.equalsIgnoreCase(PHILOSMELT_RECIPE_NAMESPACE))
            return;

        if (!PermissionManager.PHILOSTONE_TRANSITEM.check(event.getView().getPlayer()))
            return;

        boolean havePhiloStone = false;
        ItemStack[] matrixRaw = event.getInventory().getMatrix();
        for (ItemStack item : matrixRaw) {
            if (isPhilostoneItem(item)) {
                havePhiloStone = true;
                break;
            }
        }
        if  (!havePhiloStone) {
            event.getInventory().setResult(null);
            return;
        }

        ItemStack result = null;
        event.getInventory().setResult(null);

        if (namespace.equalsIgnoreCase(PHILOTRANS_RECIPE_NAMESPACE)) {
            List<ItemStack> matrix = Arrays.stream(matrixRaw)
                .filter(Objects::nonNull)
                .filter(item -> !isPhilostoneItem(item))
                .collect(Collectors.toCollection(ObjectArrayList::new));
            result = this.manager.getPhilotransResult(matrix);
        } else if  (namespace.equalsIgnoreCase(PHILOSMELT_RECIPE_NAMESPACE)) {
//            plugin.logger.info(recipe.getResult().toString());
//            plugin.logger.info(matrix.toString());
            result = this.manager.getPhilosmeltResult(recipe);
        }

        if (result == null)
            return;

        event.getInventory().setResult(result);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof Keyed))
            return;

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        for (int i = 0; i < matrix.length; ++i) {
            ItemStack item = matrix[i];
            if (!isPhilostoneItem(item))
                continue;

            final int slot = i + 1;
            final ItemStack philostone = item.clone();
            Bukkit.getScheduler().runTask(this.plugin, () -> inventory.setItem(slot, philostone));
            break;
        }
    }
}
