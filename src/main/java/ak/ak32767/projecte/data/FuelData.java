package ak.ak32767.projecte.data;


import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum FuelData {
    CHARCOAL        (1600  , new ItemWrapper.MaterialItem(Material.CHARCOAL)),
    COAL            (1600  , new ItemWrapper.MaterialItem(Material.COAL)),
    COAL_BLOCK      (16000 , new ItemWrapper.MaterialItem(Material.COAL_BLOCK)),
    ALCHEMICAL_COAL (6400  , new ItemWrapper.IAItem(Material.CHARCOAL, "projecte:alchemical_coal")),
    MOBIUS_FUEL     (25600 , new ItemWrapper.IAItem(Material.CHARCOAL, "projecte:mobius_fuel")),
    AETERNALIS_FUEL (102400, new ItemWrapper.IAItem(Material.CHARCOAL, "projecte:aeternalis_fuel"));

    private final long burnTime;
    private final ItemWrapper.TransmutableItem itemWrapped;

    FuelData(long burnTime, ItemWrapper.TransmutableItem itemWrapped) {
        this.burnTime = burnTime;
        this.itemWrapped = itemWrapped;
    }

    public long getBurnTime() {
        return this.burnTime;
    }

    public ItemWrapper.TransmutableItem getItemWrapped() {
        return this.itemWrapped;
    }

    public ItemStack getItem() {
        return this.itemWrapped.item();
    }

    public static @Nullable FuelData of (ItemStack item) {
        return of(ItemWrapper.of(item));
    }

    public static @Nullable FuelData of(ItemWrapper.TransmutableItem item) {
        return Arrays.stream(FuelData.values())
            .filter(data -> data.itemWrapped.equals(item))
            .findFirst()
            .orElse(null);
    }

    public static List<FuelData> getBurnableFuels(long targetBurnTime) {
        return Arrays.stream(FuelData.values())
            .filter(data -> data.burnTime >= targetBurnTime)
            .collect(Collectors.toCollection(ObjectArrayList::new));
    }
}
