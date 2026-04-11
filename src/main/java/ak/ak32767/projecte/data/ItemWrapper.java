package ak.ak32767.projecte.data;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class ItemWrapper {
    public static Object of(ItemStack item) {
        Material material = item.getType();

        if (!item.hasItemMeta())
            return material;

        CustomStack iaItem = CustomStack.byItemStack(item);
        if (iaItem != null)
            return new ItemWrapper.IAItem(material, iaItem.getNamespacedID());

        return new ItemWrapper.ExactItem(material, item.getItemMeta());
    }


    public static class ExactItem {
        private final Material material;
        private final ItemMeta meta;

        public ExactItem(@NotNull Material material, ItemMeta meta) {
            this.material = material;
            this.meta = meta;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;

            if (!(other instanceof ExactItem that))
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

        public Material getMaterial() {
            return this.material;
        }

        public ItemMeta getMeta() {
            return meta;
        }
    }

    public static class IAItem {
        private final Material material;
        private final String namespacedID;

        public IAItem(@NotNull Material material, @NotNull String namespacedID) {
            this.material = material;
            this.namespacedID = namespacedID;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;

            if (!(other instanceof IAItem that))
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
}
