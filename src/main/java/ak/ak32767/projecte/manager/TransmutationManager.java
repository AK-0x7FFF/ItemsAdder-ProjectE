package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.emcsys.PhiloCraftTransmutationBuilder;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;
import com.google.common.hash.Hashing;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TransmutationManager {
    private final WorldTransmutation worldTransmutation;
    private final PhiloCraftTransmutation philoCraftTransmutation;

    public TransmutationManager(ProjectE plugin) {
        this.worldTransmutation = new WorldTransmutation(plugin);
        this.philoCraftTransmutation = new PhiloCraftTransmutation(plugin);
    }

    public void build() {
        this.worldTransmutation.build();
        this.philoCraftTransmutation.build();
    }

    public WorldTransmutation getWorldTransmutation() {
        return this.worldTransmutation;
    }

    public PhiloCraftTransmutation getPhiloCraftTransmutation() {
        return this.philoCraftTransmutation;
    }


    public interface TransmutationType {
        boolean build();
        void conversionsRegister(EMCBuilder emcBuilder);
    }

    public static class PhiloCraftTransmutation implements TransmutationType {
        public static final String PHILO_RECIPE_NAMESPACE = "philofake_projecte";
        public static final String PHILOSTONE_NAMESPACEDID = "projecte:philosophers_stone";
        public static final Material PHILOSTONE_MATERIAL = Material.PHANTOM_MEMBRANE;

        public record PhiloRecipe(ItemStack resultItem, ItemStack ingredientItem, int ingredientAmount) {
            public String recipeKey() {
//                String resultName = PlainTextComponentSerializer.plainText().serialize(this.resultItem.displayName())
//                        .toLowerCase().replaceAll("[^a-z0-9/._-]", "");
//                String ingredientName = PlainTextComponentSerializer.plainText().serialize(this.ingredientItem.displayName())
//                        .toLowerCase().replaceAll("[^a-z0-9/._-]", "");
//
//                return ingredientName + "2" + resultName;
                String raw = this.getItemID(resultItem) + resultItem.getAmount() + this.getItemID(ingredientItem) + this.ingredientAmount;
                return Hashing.murmur3_32_fixed().hashString(raw, StandardCharsets.US_ASCII).toString();
            }

            private String getItemID(ItemStack item) {
                CustomStack iaItem = CustomStack.byItemStack(item);
                return iaItem == null ? item.getType().getKey().getNamespace() + item.getType().getKey().getKey() : iaItem.getNamespacedID();
            }

            public Recipe toRecipe() {
                ShapelessRecipe recipe = new ShapelessRecipe(
                        new NamespacedKey(PHILO_RECIPE_NAMESPACE, this.recipeKey()), this.resultItem
                );
                recipe.addIngredient(PhiloCraftTransmutation.getPhilostoneItem());

                RecipeChoice choice;
                if (ItemWrapper.of(this.ingredientItem) instanceof ItemWrapper.IAItem)
                    choice = new RecipeChoice.MaterialChoice(this.ingredientItem.getType());
                else
                    choice = new RecipeChoice.ExactChoice(this.ingredientItem);

                for (int i = 0; i < this.ingredientAmount; ++i)
                    recipe.addIngredient(choice);

                return recipe;
            }
        }

        private final ProjectE plugin;
        private final PhiloCraftTransmutationBuilder builder;
        private final Map<String, PhiloRecipe> registeredRecipes;

        private PhiloCraftTransmutation(ProjectE plugin) {
            this.plugin = plugin;
            this.builder = new PhiloCraftTransmutationBuilder(plugin);
            this.registeredRecipes = new Object2ObjectOpenHashMap<>();
        }

        public static ItemStack getPhilostoneItem() {
            return CustomStack.getInstance(PHILOSTONE_NAMESPACEDID).getItemStack();
        }

        @Override
        public boolean build() {
            return this.builder.build();
        }

        public void register(String recipeKey, PhiloRecipe recipe) {
        this.registeredRecipes.put(recipeKey, recipe);
    }

        @Override
        public void conversionsRegister(EMCBuilder emcBuilder) {
            for (var data : this.builder.getConversions()) {
                emcBuilder
                    .register(data.itemA(), data.amountA()).addIngredient(data.itemB(),  data.amountB()).end()
                    .register(data.itemB(), data.amountB()).addIngredient(data.itemA(),  data.amountA()).end();
            }
        }

        public Set<Recipe> conversions2Recipes() {
            Set<Recipe> recipes = new ObjectLinkedOpenHashSet<>(this.builder.getConversions().size() << 1);

            for (var data : this.builder.getConversions()) {
                for (PhiloRecipe recipe : data.toPhiloRecipes()) {
                    recipes.add(recipe.toRecipe());
                    this.register(recipe.recipeKey(), recipe);
                }
            }

            return recipes;
        }

        public @Nullable ItemStack getResultByMatrix(List<ItemStack> items) {
//        plugin.logger.info(items.toString());
            int amount = 0;
            ItemStack targetItem = null;
            for (ItemStack item : items) {
                CustomStack iaItem = CustomStack.byItemStack(item);

                if (iaItem != null) {
                    // 賢者之石排除
                    if (item.getType().equals(PHILOSTONE_MATERIAL) && iaItem.getNamespacedID().equals(PHILOSTONE_NAMESPACEDID))
                        continue;

                    item = iaItem.getItemStack();
                }

                if (targetItem == null)
                    targetItem = item;
                else if (!item.isSimilar(targetItem))
                    return null;

                amount++;
            }

            ItemStack finalTargetItem = targetItem;
            int finalAmount = amount;

            Optional<PhiloRecipe> targetRecipeFind = this.registeredRecipes.values().stream().filter(
                recipe -> finalTargetItem.isSimilar(recipe.ingredientItem()) && finalAmount == recipe.ingredientAmount()
            ).findFirst();
            if (targetRecipeFind.isEmpty())
                return null;

            PhiloRecipe targetRecipe = targetRecipeFind.get();
            plugin.logger.info("RESULT: " + targetRecipe.resultItem().toString());
            plugin.logger.info("NEEEED: (" + targetRecipe.ingredientAmount() + ") " + targetRecipe.ingredientItem().toString());
            return targetRecipe.resultItem();
        }
    }

    public static class WorldTransmutation implements TransmutationType {
        private final ProjectE plugin;
        private final WorldTransmutationsBuilder builder;

        private WorldTransmutation(ProjectE plugin) {
            this.plugin = plugin;
            this.builder = new WorldTransmutationsBuilder(plugin);
        }

        @Override
        public boolean build() {
            return this.builder.build();
        }

        @Override
        public void conversionsRegister(EMCBuilder emcBuilder) {
            for (var node: this.builder.getConversions()) {
                emcBuilder.register(node.resultForward(), 1).addIngredient(node.origin()).end();

                if (!node.resultForward().equals(node.resultBackward()))
                    emcBuilder.register(node.resultBackward(), 1).addIngredient(node.origin()).end();
            }
        }
    }
}
