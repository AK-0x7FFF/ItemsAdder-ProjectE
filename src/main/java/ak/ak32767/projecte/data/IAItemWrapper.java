package ak.ak32767.projecte.data;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IAItemWrapper {
    private final Material material;
    private final String namespacedID;

    public IAItemWrapper(@NotNull Material material, @NotNull String namespacedID) {
        this.material = material;
        this.namespacedID = namespacedID;
    }

    public static Object of(ItemStack item) {
        Material material = item.getType();

        if (!item.hasItemMeta())
            return material;

        CustomStack iaItem = CustomStack.byItemStack(item);
        if (iaItem != null)
            return new IAItemWrapper(material, iaItem.getNamespacedID());

        return new ExactItemWrapper(material, item.getItemMeta());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof IAItemWrapper that))
            return false;

        return this.namespacedID.equals(that.namespacedID);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + this.material.hashCode();
        hash = 19 * hash + this.namespacedID.hashCode();

        return hash;
    }

    public Material getMaterial() {
        return material;
    }

    public String getNamespacedID() {
        return namespacedID;
    }
}
