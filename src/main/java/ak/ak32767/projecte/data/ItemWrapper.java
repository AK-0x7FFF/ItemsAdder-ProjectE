package ak.ak32767.projecte.data;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;



public class ItemWrapper {
    private ItemWrapper() {}

    public interface TransmutableItem {
        @Override
        boolean equals(Object other);

        @Override
        int hashCode();

        Material material();
        ItemStack item();
    }

    public enum Transmutable {
        MATERIAL_ITEM,
        EXACT_ITEM,
        IAITEM;

        public static Transmutable get(ItemStack item) {
            if (!item.hasItemMeta())
                return MATERIAL_ITEM;

            if (CustomStack.byItemStack(item) != null)
                return IAITEM;

            return EXACT_ITEM;
        }

        public boolean is(ItemStack item) {
            return switch (this) {
                case MATERIAL_ITEM -> !item.hasItemMeta();

                case EXACT_ITEM -> {
                    if (!item.hasItemMeta())
                        yield false;

                    CustomStack iaItem = CustomStack.byItemStack(item);
                    yield iaItem == null;
                }

                case IAITEM -> {
                    if (!item.hasItemMeta())
                        yield false;

                    CustomStack iaItem = CustomStack.byItemStack(item);
                    yield iaItem != null;
                }
            };
        }

        public boolean is(TransmutableItem item) {
            return switch (this) {
                case MATERIAL_ITEM -> item instanceof MaterialItem;
                case EXACT_ITEM -> item instanceof ExactItem;
                case IAITEM -> item instanceof IAItem;
            };
        }
    }

    public static TransmutableItem of(ItemStack item) {
        Material material = item.getType();

        return switch (Transmutable.get(item)) {
            case Transmutable.MATERIAL_ITEM ->
                new MaterialItem(material);
            case Transmutable.EXACT_ITEM ->
                new ExactItem(material, item.getItemMeta());
            case Transmutable.IAITEM ->
                new IAItem(material, CustomStack.byItemStack(item).getNamespacedID());
        };
    }

    public static MaterialItem toMaterialItemWrapper(TransmutableItem item) {
        return new MaterialItem(item.material());
    }

    public record MaterialItem(Material material) implements TransmutableItem {
        public MaterialItem(ItemStack item) {
            this(item.getType());
        }

        @Override
        public ItemStack item() {
            if (!this.material.isItem())
                return ItemStack.empty();
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
            if (!this.material.isItem())
                return ItemStack.empty();

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

    public record IAItem(Material material, String namespacedID) implements TransmutableItem {
        public IAItem(ItemStack item) {
            this(item.getType(), CustomStack.byItemStack(item).getNamespacedID());
        }

        public IAItem(CustomStack iaItem) {
            this(iaItem.getItemStack());
        }

        public ItemStack item() {
            return CustomStack.getInstance(this.namespacedID).getItemStack();
        }
    }
}
