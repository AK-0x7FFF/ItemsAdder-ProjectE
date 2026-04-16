package ak.ak32767.projecte.data;

import ak.ak32767.projecte.emcsys.PhiloCraftTransmutationBuilder;
import com.google.common.hash.Hashing;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class PhilotransRecipe extends philoRecipe {
    public static final String PHILOTRANS_RECIPE_NAMESPACE = "philotrans_projecte";

    private final ItemStack result;
    private final ItemStack ingredientItem;
    private final int ingredientAmount;
    private final NamespacedKey key;

    public PhilotransRecipe(ItemStack result, ItemStack ingredientItem, int ingredientAmount) {
        this.result = result.clone();
        this.ingredientItem = ingredientItem.clone();
        this.ingredientAmount = ingredientAmount;

        String keyRaw = getItemNamespacedID(result) + result.getAmount() + getItemNamespacedID(ingredientItem) + ingredientAmount;
        String key = Hashing.murmur3_32_fixed().hashString(keyRaw, StandardCharsets.US_ASCII).toString();
        this.key = new NamespacedKey(PHILOTRANS_RECIPE_NAMESPACE, key);
    }

    public static Set<PhilotransRecipe> of(PhiloCraftTransmutationBuilder.PhiloCraftTransmutationData data) {
        Set<PhilotransRecipe> philotransRecipes = new ObjectLinkedOpenHashSet<>(2);

        ItemStack resultItem; {
            resultItem = data.itemA().item();
            resultItem.setAmount((int) data.amountA());
            philotransRecipes.add(new PhilotransRecipe(resultItem, data.itemB().item(), (int) data.amountB()));

            resultItem = data.itemB().item();
            resultItem.setAmount((int) data.amountB());
            philotransRecipes.add(new PhilotransRecipe(resultItem, data.itemA().item(), (int) data.amountA()));
        }

        return philotransRecipes;
    }

    public ItemStack getIngredientItem() {
        return this.ingredientItem;
    }

    public int getIngredientAmount() {
        return this.ingredientAmount;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public @NotNull ItemStack getResult() {
        return this.result;
    }

    public ShapelessRecipe toRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(this.getKey(), this.result);
        recipe.addIngredient(PHILOSTONE_MATERIAL);

        RecipeChoice choice;
        if (ItemWrapper.of(this.ingredientItem) instanceof ItemWrapper.IAItem)
            choice = new RecipeChoice.MaterialChoice(this.ingredientItem.getType());
        else
            choice = new RecipeChoice.ExactChoice(this.ingredientItem);

        for (int i = 0; i < this.ingredientAmount; ++i)
            recipe.addIngredient(choice);

        return recipe;
    }

    public boolean isSimilar(List<ItemStack> items) {
        int amount = 0;
        for (ItemStack item : items) {
//            if (isPhilostoneItem(item))
//                continue;

            CustomStack iaItem = CustomStack.byItemStack(item);
            if (iaItem != null)
                item = iaItem.getItemStack();

            if (!item.isSimilar(this.ingredientItem))
                return false;

            amount++;
        }

        return amount == this.ingredientAmount;
    }
}
