package ak.ak32767.projecte.emcsys;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Set;

public class ConversionBuilder<PARENT> {
    private static final Set<Object> BLANK = Collections.singleton(Material.AIR);

    private final PARENT parent;
    private final Object result;
    private final long amount;
    private final Object2LongLinkedOpenHashMap<Set<Object>> ingredients;

    public ConversionBuilder(PARENT emcBuilder, Object result, long amount) {
        this.parent = emcBuilder;
        this.result = result;
        this.amount = amount;
        this.ingredients = new Object2LongLinkedOpenHashMap<>();
        this.ingredients.defaultReturnValue(0);
    }

    public Object getResult() {
        return result;
    }

    public long getAmount() {
        return amount;
    }

    public final Object2LongLinkedOpenHashMap<Set<Object>> getIngredients() {
        return ingredients;
    }

    public ConversionBuilder<PARENT> addValue(long value) {
        this.ingredients.addTo(BLANK, value);
        return this;
    }

    public ConversionBuilder<PARENT> addIngredient(Object ingredient) {
        return this.addIngredient(ingredient, 1);
    }

    public ConversionBuilder<PARENT> addIngredient(Object ingredient, long amount) {
        this.ingredients.addTo(Collections.singleton(ingredient), amount);
        return this;
    }

    public ConversionBuilder<PARENT> addIngredientsGroup(Set<Object> ingredients) {
        return this.addIngredientsGroup(ingredients, 1);
    }

    public ConversionBuilder<PARENT> addIngredientsGroup(Set<Object> ingredients, long amount) {
        Set<Object> immutSet = Set.copyOf(ingredients);
        this.ingredients.addTo(immutSet, amount);
        return this;
    }

    public PARENT end() {
        return parent;
    }
}
