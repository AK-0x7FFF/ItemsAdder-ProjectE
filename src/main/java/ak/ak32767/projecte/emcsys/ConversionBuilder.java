package ak.ak32767.projecte.emcsys;

import ak.ak32767.projecte.data.ItemWrapper;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Set;

public class ConversionBuilder<PARENT> {
    private static final Set<ItemWrapper.TransmutableItem> BLANK = Collections.singleton(new ItemWrapper.MaterialItem(Material.AIR));

    private final PARENT parent;
    private final ItemWrapper.TransmutableItem result;
    private final long amount;
    private final Object2LongLinkedOpenHashMap<Set<ItemWrapper.TransmutableItem>> ingredients;

    public ConversionBuilder(PARENT emcBuilder, ItemWrapper.TransmutableItem result, long amount) {
        this.parent = emcBuilder;
        this.result = result;
        this.amount = amount;
        this.ingredients = new Object2LongLinkedOpenHashMap<>();
        this.ingredients.defaultReturnValue(0);
    }

    public ItemWrapper.TransmutableItem getResult() {
        return result;
    }

    public long getAmount() {
        return amount;
    }

    public final Object2LongLinkedOpenHashMap<Set<ItemWrapper.TransmutableItem>> getIngredients() {
        return ingredients;
    }

    public ConversionBuilder<PARENT> addValue(long value) {
        this.ingredients.addTo(BLANK, value);
        return this;
    }

    public ConversionBuilder<PARENT> addIngredient(ItemWrapper.TransmutableItem ingredient) {
        return this.addIngredient(ingredient, 1);
    }

    public ConversionBuilder<PARENT> addIngredient(ItemWrapper.TransmutableItem ingredient, long amount) {
        this.ingredients.addTo(Collections.singleton(ingredient), amount);
        return this;
    }

    public ConversionBuilder<PARENT> addIngredientsGroup(Set<ItemWrapper.TransmutableItem> ingredients) {
        return this.addIngredientsGroup(ingredients, 1);
    }

    public ConversionBuilder<PARENT> addIngredientsGroup(Set<ItemWrapper.TransmutableItem> ingredients, long amount) {
        Set<ItemWrapper.TransmutableItem> immutSet = Set.copyOf(ingredients);
        this.ingredients.addTo(immutSet, amount);
        return this;
    }

    public PARENT end() {
        return parent;
    }
}
