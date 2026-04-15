package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.emcsys.PhiloCraftTransmutationBuilder;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PhiloRecipeManager {
    public static final String PHILO_RECIPE_NAMESPACE = "philofake_projecte";
    public static final String PHILOSTONE_NAMESPACEDID = "projecte:philosophers_stone";
    public static final Material PHILOSTONE_MATERIAL = Material.PHANTOM_MEMBRANE;
    public record PhiloRecipe(ItemStack resultItem, ItemStack ingredientItem, int ingredientAmount) {}

    private final ProjectE plugin;
    private final Map<String, PhiloRecipe> registered;

    public PhiloRecipeManager(ProjectE plugin) {
        this.plugin = plugin;
        this.registered = new Object2ObjectOpenHashMap<>();
    }

    public static ItemStack getPhilostoneItem() {
        return CustomStack.getInstance(PHILOSTONE_NAMESPACEDID).getItemStack();
    }

    public PhiloRecipeManager register(String recipeKey, ItemStack result, ItemStack ingredientItem, int ingredientAmount) {
        this.registered.put(recipeKey, new PhiloRecipe(result, ingredientItem, ingredientAmount));
        return this;
    }

    public @Nullable ItemStack transmutationRecipe(List<ItemStack> items) {
//        PhiloRecipe targetRecipe = this.registered.get(recipeKey);
//        if (targetRecipe == null)
//            return null;

//        plugin.logger.info("TARGET: " + recipeKey);
//        plugin.logger.info("TARGET: (" + targetRecipe.ingredientAmount + ") " + targetRecipe.ingredientItem);
        plugin.logger.info(items.toString());


        int amount = 0;
        ItemStack targetItem = null;
        for (ItemStack item : items) {
            CustomStack iaItem = CustomStack.byItemStack(item);

            if (iaItem != null) {
                // 賢者之石排除
                if (item.getType().equals(PHILOSTONE_MATERIAL) && iaItem.getNamespacedID().equals(PhiloRecipeManager.PHILOSTONE_NAMESPACEDID))
                    continue;

                item = iaItem.getItemStack();
            }

            if (targetItem == null)
                targetItem = item;
            else if (!item.isSimilar(targetItem))
                return null;
//            plugin.logger.info(amount + " " + item.isSimilar(targetRecipe.ingredientItem()) + " " + item.toString());

            amount++;
        }
        if (targetItem == null)
            return null;

        ItemStack finalTargetItem = targetItem;
        int finalAmount = amount;

        Optional<PhiloRecipe> targetRecipeFind = this.registered.values().stream().filter(
            recipe -> finalTargetItem.isSimilar(recipe.ingredientItem()) && finalAmount == recipe.ingredientAmount()
        ).findFirst();

        if (targetRecipeFind.isEmpty())
            return null;
        PhiloRecipe targetRecipe = targetRecipeFind.get();

//        plugin.logger.info(targetRecipe.ingredientAmount() + " " + amount);
//        if (amount != targetRecipe.ingredientAmount())
//            return null;

        plugin.logger.info("RESULT: " + targetRecipe.resultItem().toString());
        plugin.logger.info("NEEEED: (" + targetRecipe.ingredientAmount() + ") " + targetRecipe.ingredientItem().toString());
        return targetRecipe.resultItem();
    }

    public Recipe a(String recipeKey, PhiloRecipe philoRecipe) {
        ShapelessRecipe recipe = new ShapelessRecipe(
                new NamespacedKey(PhiloRecipeManager.PHILO_RECIPE_NAMESPACE, recipeKey), philoRecipe.resultItem()
        );
        recipe.addIngredient(getPhilostoneItem());

        RecipeChoice choice;
        if (ItemWrapper.of(philoRecipe.ingredientItem()) instanceof ItemWrapper.IAItem)
            choice = new RecipeChoice.MaterialChoice(philoRecipe.ingredientItem().getType());
        else
            choice = new RecipeChoice.ExactChoice(philoRecipe.ingredientItem());
        for (int i = 0; i < philoRecipe.ingredientAmount(); ++i) {
            recipe.addIngredient(choice);
        }
        return recipe;
    }
}
