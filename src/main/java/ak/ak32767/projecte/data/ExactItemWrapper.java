package ak.ak32767.projecte.data;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ExactItemWrapper {
//    private final NamespacedKey IA_KEY = new NamespacedKey("itemsadder", "id");

    private final Material material;
    private final ItemMeta meta;

    public ExactItemWrapper(@NotNull Material material, ItemMeta meta) {
        this.material = material;
        this.meta = meta;
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

        if (!(other instanceof ExactItemWrapper that))
            return false;

        if (this.material == that.material)
            return true;

        // 耐久度過濾
        if (this.meta instanceof Damageable && that.meta instanceof Damageable) {
            ItemMeta thisMeta = this.meta.clone();
            ItemMeta thatMeta = that.meta.clone();
            ((Damageable) thisMeta).setDamage(0);
            ((Damageable) thatMeta).setDamage(0);

            return Objects.equals(thisMeta, thatMeta);
        }

        return Objects.equals(this.meta, that.meta);
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 19 * hash + (this.material != null ? this.material.hashCode() : 0);
        hash = 19 * hash + (this.meta != null ? this.meta.hashCode() : 0);

        return hash;
    }

//    public boolean isIAItem() {
//        return this.meta.getPersistentDataContainer().has(IA_KEY, PersistentDataType.STRING);
//    }

    public Material getMaterial() {
        return this.material;
    }

    public ItemMeta getMeta() {
        return meta;
    }
}
