package ak.ak32767.projecte.data;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;



public class ItemWrapper {
    private ItemWrapper() {};

    public static TransmutableItem of(ItemStack item) {
        Material material = item.getType();

        if (!item.hasItemMeta())
            return new MaterialItem(material);

        CustomStack iaItem = CustomStack.byItemStack(item);
        if (iaItem != null)
            return new IAItem(material, iaItem.getNamespacedID());

        return new ExactItem(material, item.getItemMeta());
    }

    public static MaterialItem toMaterialItem(TransmutableItem item) {
        return new MaterialItem(item.material());
    }

    public static boolean isMaterialItem(ItemStack item) {
        return !item.hasItemMeta();
    }

    public static boolean isIAItem(ItemStack item) {
        if (!item.hasItemMeta())
            return false;

        CustomStack iaItem = CustomStack.byItemStack(item);
        return iaItem != null;
    }

    public static boolean isExactItem(ItemStack item) {
        if (!item.hasItemMeta())
            return false;

        CustomStack iaItem = CustomStack.byItemStack(item);
        return iaItem == null;
    }


    public interface TransmutableItem {
        @Override
        boolean equals(Object other);

        @Override
        int hashCode();

        Material material();
        ItemStack item();
    }

    public record MaterialItem(Material material) implements TransmutableItem {
        @Override
        public ItemStack item() {
            return ItemStack.of(this.material);
        }
    }

    public record ExactItem(@NotNull Material material, ItemMeta meta) implements TransmutableItem {
        public ExactItem(@NotNull ItemStack item) {
            this(item.getType(), item.getItemMeta());
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;

            if (!(other instanceof ExactItem that))
                return false;

            if (this.material != that.material)
                return false;

            ItemMeta thisMeta = normalize(this.meta);
            ItemMeta thatMeta = normalize(that.meta);

            return thisMeta.equals(thatMeta);

        }

        @Override
        public int hashCode() {

            int hash = 3;
            hash = 19 * hash + this.material.hashCode();
            hash = 19 * hash + (this.meta != null ? normalize(this.meta).hashCode() : 0);

            return hash;
        }

        public ItemStack item() {
            ItemStack item = ItemStack.of(this.material);

            if (this.meta != null)
                item.setItemMeta(this.meta);

            return item;
        }

        private static ItemMeta normalize(ItemMeta input) {
            if (!(input instanceof Damageable))
                return input;

            ItemMeta clone = input.clone();
            // 耐久度過濾
            ((Damageable) clone).setDamage(0);

            return clone;
        }
    }

    public record IAItem(@NotNull Material material, @NotNull String namespacedID) implements TransmutableItem {
        public IAItem(@NotNull ItemStack item) {
            this(item.getType(), CustomStack.byItemStack(item).getNamespacedID());
        }

        public ItemStack item() {
            return CustomStack.getInstance(this.namespacedID).getItemStack();
        }
    }
}
