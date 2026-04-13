package ak.ak32767.projecte.event;

import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.emcsys.ItemConversionBuilder;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EMCPreCalculateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final EMCBuilder builder;

    public EMCPreCalculateEvent(EMCBuilder builder) {
        this.builder = builder;
    }

    public EMCBuilder getBuilder() {
        return builder;
    }

    public boolean addFixedEMCItem(ItemStack item, BigInteger emcValue) {
        ItemWrapper.TransmutableItem itemWrapped = ItemWrapper.of(item);
        this.builder.fixed(itemWrapped, new BigDecimal(emcValue));
        return true;
    }

    public boolean conversionRecipe(Recipe recipe) {
        return this.builder.addConversionByRecipe(recipe);
    }

    public ItemConversionBuilder conversionItemBuilder(ItemStack item) {
        return this.conversionItemBuilder(item, 1);
    }

    public ItemConversionBuilder conversionItemBuilder(ItemStack item, long amount) {
        return this.builder.register(item, amount);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
