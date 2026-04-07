package ak.ak32767.projecte.emcsys;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Set;

public class ConversionBuilder<PARENT> {
    private static final Set<Material> BLANK = Collections.singleton(Material.AIR);

    private final PARENT parent;
    private final Material material;
    private final long amount;
    private final Object2LongLinkedOpenHashMap<Set<Material>> ingredients;

    public ConversionBuilder(PARENT emcBuilder, Material materials, long amount) {
        this.parent = emcBuilder;
        this.material = materials;
        this.amount = amount;
        this.ingredients = new Object2LongLinkedOpenHashMap<>();
        this.ingredients.defaultReturnValue(0);
    }

    public Material getMaterial() {
        return material;
    }

    public long getAmount() {
        return amount;
    }

    public final Object2LongLinkedOpenHashMap<Set<Material>> getIngredients() {
        return ingredients;
    }

    public ConversionBuilder<PARENT> addValue(long value) {
        this.ingredients.addTo(BLANK, value);
        return this;
    }

    public ConversionBuilder<PARENT> addIngredient(Material ingredient) {
        this.ingredients.addTo(Collections.singleton(ingredient), 1);
        return this;
    }

    public ConversionBuilder<PARENT> addIngredient(Material ingredient, long amount) {
        this.ingredients.addTo(Collections.singleton(ingredient), amount);
        return this;
    }

    public ConversionBuilder<PARENT> addIngredientsGroup(Set<Material> ingredients) {
        Set<Material> immutSet = Set.copyOf(ingredients);
        this.ingredients.addTo(immutSet, 1);
        return this;
    }

    public ConversionBuilder<PARENT> addIngredientsGroup(Set<Material> ingredients, long amount) {
        Set<Material> immutSet = Set.copyOf(ingredients);
        this.ingredients.addTo(immutSet, amount);
        return this;
    }

    public PARENT end() {
        return parent;
    }
}
