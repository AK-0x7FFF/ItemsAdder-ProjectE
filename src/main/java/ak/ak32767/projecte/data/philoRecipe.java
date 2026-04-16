package ak.ak32767.projecte.data;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public abstract class philoRecipe implements Keyed {
    public static final Material PHILOSTONE_MATERIAL = Material.PHANTOM_MEMBRANE;

    protected static String getItemNamespacedID(ItemStack item) {
        CustomStack iaItem = CustomStack.byItemStack(item);
        return iaItem == null ? item.getType().getKey().getNamespace() + item.getType().getKey().getKey() : iaItem.getNamespacedID();
    }

    public abstract Recipe toRecipe();
    public abstract ItemStack getResult();
}
