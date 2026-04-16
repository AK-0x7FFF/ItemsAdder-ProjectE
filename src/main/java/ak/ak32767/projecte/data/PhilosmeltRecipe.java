package ak.ak32767.projecte.data;

import com.google.common.hash.Hashing;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class PhilosmeltRecipe extends philoRecipe {
    public static final String PHILOSMELT_RECIPE_NAMESPACE = "philosmelt_projecte";

    private final ItemStack result;
    private final RecipeChoice ingredient;
//    private final long cookTime;
    private final NamespacedKey key;

    public PhilosmeltRecipe(ItemStack result, RecipeChoice ingredient) {
//    public PhilosmeltRecipe(ItemStack result, RecipeChoice ingredient, long cookTime) {
        this.result = result.clone();
        this.result.setAmount(7);
        this.ingredient = ingredient.clone();
//        this.cookTime = cookTime * 7;

        String key = Hashing.murmur3_32_fixed().hashString(getItemNamespacedID(result), StandardCharsets.US_ASCII).toString();
        this.key = new NamespacedKey(PHILOSMELT_RECIPE_NAMESPACE, key);
    }

    public PhilosmeltRecipe(FurnaceRecipe recipe) {
        this(recipe.getResult(), recipe.getInputChoice());
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public @NotNull ItemStack getResult() {
        return this.result;
    }

    public RecipeChoice getIngredient() {
        return this.ingredient;
    }

//    public Set<ItemStack> getBurnableFuels() {
//        return FuelData.getBurnableFuels(this.cookTime).stream()
//            .map(FuelData::getItem)
//            .collect(Collectors.toCollection(ObjectOpenHashSet::new));
//    }

    public ShapelessRecipe toRecipe() {
//        List<Material> burnableFuels = FuelData.getBurnableFuels(this.cookTime).stream()
//                .map(data -> data.getItemWrapped().material())
//                .collect(Collectors.toCollection(ObjectArrayList::new));
//        if (burnableFuels.isEmpty())
//            return null;
        ShapelessRecipe recipe = new ShapelessRecipe(this.key, this.result);
        recipe.addIngredient(PHILOSTONE_MATERIAL);

        List<Material> fuels = Arrays.stream(FuelData.values())
                .map(data -> data.getItem().getType())
                .collect(Collectors.toCollection(ObjectArrayList::new));
        RecipeChoice.MaterialChoice fuelChoice = new RecipeChoice.MaterialChoice(fuels);
        recipe.addIngredient(fuelChoice);

        // 原料
        recipe.addIngredient(this.ingredient);
        recipe.addIngredient(this.ingredient);
        recipe.addIngredient(this.ingredient);
        recipe.addIngredient(this.ingredient);
        recipe.addIngredient(this.ingredient);
        recipe.addIngredient(this.ingredient);
        recipe.addIngredient(this.ingredient);

        return recipe;
    }
}
