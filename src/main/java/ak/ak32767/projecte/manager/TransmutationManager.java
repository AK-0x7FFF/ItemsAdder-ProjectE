package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.FuelData;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.emcsys.PhiloCraftTransmutationBuilder;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;
import com.google.common.hash.Hashing;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
        public static final String PHILOTRANS_RECIPE_NAMESPACE = "philotrans_projecte";
        public static final String PHILOSMELT_RECIPE_NAMESPACE = "philosmelt_projecte";
        public static final String PHILOSTONE_NAMESPACEDID = "projecte:philosophers_stone";
        public static final Material PHILOSTONE_MATERIAL = Material.PHANTOM_MEMBRANE;

        public record PhilotransRecipe(ItemStack resultItem, ItemStack ingredientItem, int ingredientAmount) {
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
                        new NamespacedKey(PHILOTRANS_RECIPE_NAMESPACE, this.recipeKey()), this.resultItem
                );
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
        }

        private final ProjectE plugin;
        private final PhiloCraftTransmutationBuilder builder;
        private final Map<String, PhilotransRecipe> registeredPhilotransRecipes;
        private final Map<Recipe, Long> registeredPhilosmeltRecipes;

        private PhiloCraftTransmutation(ProjectE plugin) {
            this.plugin = plugin;
            this.builder = new PhiloCraftTransmutationBuilder(plugin);
            this.registeredPhilotransRecipes = new Object2ObjectOpenHashMap<>();
            this.registeredPhilosmeltRecipes = new Object2LongOpenHashMap<>();
        }

        public static ItemStack getPhilostoneItem() {
            return CustomStack.getInstance(PHILOSTONE_NAMESPACEDID).getItemStack();
        }

        public static boolean isPhilostoneItem(ItemStack item) {
            if (item == null || item.isEmpty())
                return false;

            if (!item.getType().equals(PHILOSTONE_MATERIAL))
                return false;

            CustomStack iaItem = CustomStack.byItemStack(item);
            if  (iaItem == null)
                return false;

            return iaItem.getNamespacedID().equals(PHILOSTONE_NAMESPACEDID);
        }

        @Override
        public boolean build() {
            this.a();
            return this.builder.build();
        }

        public void registerPhilotransRecipe(String recipeKey, PhilotransRecipe recipe) {
        this.registeredPhilotransRecipes.put(recipeKey, recipe);
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
                for (PhilotransRecipe recipe : data.toPhilotransRecipes()) {
                    recipes.add(recipe.toRecipe());
                    this.registerPhilotransRecipe(recipe.recipeKey(), recipe);
                }
            }

            return recipes;
        }

        public @Nullable ItemStack getPhilotransResult(List<ItemStack> items) {
//        plugin.logger.info(items.toString());
            int amount = 0;
            ItemStack targetItem = null;
            for (ItemStack item : items) {
                if (isPhilostoneItem(item))
                    continue;

                CustomStack iaItem = CustomStack.byItemStack(item);
                if (iaItem != null)
                    item = iaItem.getItemStack();

                if (targetItem == null)
                    targetItem = item;
                else if (!item.isSimilar(targetItem))
                    return null;

                amount++;
            }

            ItemStack finalTargetItem = targetItem;
            int finalAmount = amount;

            Optional<PhilotransRecipe> targetRecipeFind = this.registeredPhilotransRecipes.values().stream().filter(
                recipe -> finalTargetItem.isSimilar(recipe.ingredientItem()) && finalAmount == recipe.ingredientAmount()
            ).findFirst();
            if (targetRecipeFind.isEmpty())
                return null;

            PhilotransRecipe targetRecipe = targetRecipeFind.get();
            plugin.logger.info("RESULT: " + targetRecipe.resultItem().toString());
            plugin.logger.info("NEEEED: (" + targetRecipe.ingredientAmount() + ") " + targetRecipe.ingredientItem().toString());
            return targetRecipe.resultItem();
        }

        public void a() {
            for (Iterator<Recipe> recipes = Bukkit.recipeIterator(); recipes.hasNext(); ) {
                Recipe recipe = recipes.next();

                if (!(recipe instanceof Keyed))
                    continue;

                if (!(recipe instanceof CookingRecipe<?>))
                    continue;

                long targetBurnTime = ((CookingRecipe<?>) recipe).getCookingTime() * 7L;
                List<Material> burnableFuels = FuelData.getBurnableFuels(targetBurnTime).stream()
                        .map(data -> data.getItemWrapped().material())
                        .collect(Collectors.toCollection(ObjectArrayList::new));
                if (burnableFuels.isEmpty())
                    continue;

                ItemStack result = recipe.getResult().clone();
                result.setAmount(7);
                ShapelessRecipe philoRecipe = new ShapelessRecipe(
                    new NamespacedKey(PHILOSMELT_RECIPE_NAMESPACE, ((CookingRecipe<?>) recipe).getKey().getKey()),
                    result
                );
                philoRecipe.addIngredient(PHILOSTONE_MATERIAL);

                RecipeChoice.MaterialChoice fuelChoice = new RecipeChoice.MaterialChoice(burnableFuels);
                philoRecipe.addIngredient(fuelChoice);

                RecipeChoice input = ((CookingRecipe<?>) recipe).getInputChoice();
                philoRecipe.addIngredient(input);
                philoRecipe.addIngredient(input);
                philoRecipe.addIngredient(input);
                philoRecipe.addIngredient(input);
                philoRecipe.addIngredient(input);
                philoRecipe.addIngredient(input);
                philoRecipe.addIngredient(input);

                Bukkit.addRecipe(philoRecipe);
                this.registeredPhilosmeltRecipes.put(philoRecipe, targetBurnTime);
            }

        }

        public @Nullable ItemStack getPhilosmeltResult(Recipe recipe, List<ItemStack> items) {
            return recipe.getResult().clone();
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
