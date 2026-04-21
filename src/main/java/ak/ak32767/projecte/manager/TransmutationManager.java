package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.PhilosmeltRecipe;
import ak.ak32767.projecte.data.PhilotransRecipe;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.emcsys.PhiloCraftTransmutationBuilder;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static ak.ak32767.projecte.data.philoRecipe.PHILOSTONE_MATERIAL;

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
        public static final String PHILOSTONE_NAMESPACEDID = "projecte:philosophers_stone";

        private final ProjectE plugin;
        private final PhiloCraftTransmutationBuilder builder;
        private final Set<PhilotransRecipe> registeredPhilotransRecipes;
        private final Map<NamespacedKey, PhilosmeltRecipe> registeredPhilosmeltRecipes;

        private PhiloCraftTransmutation(ProjectE plugin) {
            this.plugin = plugin;
            this.builder = new PhiloCraftTransmutationBuilder(plugin);
            this.registeredPhilotransRecipes = new ObjectOpenHashSet<>();
            this.registeredPhilosmeltRecipes = new Object2ObjectOpenHashMap<>();
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
            return this.builder.build();
        }

        @Override
        public void conversionsRegister(EMCBuilder emcBuilder) {
            for (var data : this.builder.getConversions()) {
                emcBuilder
                    .conversion(data.itemA(), data.amountA()).addIngredient(data.itemB(),  data.amountB()).end()
                    .conversion(data.itemB(), data.amountB()).addIngredient(data.itemA(),  data.amountA()).end();
            }
        }

        public void registerPhilotransRecipes() {
            for (var data : this.builder.getConversions()) {
                for (PhilotransRecipe philoRecipe : PhilotransRecipe.of(data)) {
                    ShapelessRecipe recipe = philoRecipe.toRecipe();

                    Bukkit.removeRecipe(recipe.getKey());
                    Bukkit.addRecipe(recipe);
                    this.registeredPhilotransRecipes.add(philoRecipe);
                }
            }

            this.plugin.logger.info(this.registeredPhilotransRecipes.size() + " Philotrans Recipes been Registered.");
        }

        public @Nullable ItemStack getPhilotransResult(List<ItemStack> items) {
            Optional<PhilotransRecipe> targetRecipeFind = this.registeredPhilotransRecipes.stream()
                .filter(philoRecipe -> philoRecipe.isSimilar(items))
                .findFirst();
            if (targetRecipeFind.isEmpty())
                return null;

            PhilotransRecipe targetRecipe = targetRecipeFind.get();
            plugin.logger.info("RESULT: " + targetRecipe.getResult());
            plugin.logger.info("NEEEED: (" + targetRecipe.getIngredientAmount() + ") " + targetRecipe.getIngredientItem().toString());
            return targetRecipe.getResult();
        }

        public void registerPhilosmeltRecipes() {
            this.registeredPhilosmeltRecipes.clear();

            for (Iterator<Recipe> recipes = Bukkit.recipeIterator(); recipes.hasNext(); ) {
                Recipe recipe = recipes.next();

                if (!(recipe instanceof Keyed))
                    continue;

                if (!(recipe instanceof FurnaceRecipe))
                    continue;

                PhilosmeltRecipe philosmeltRecipe = new PhilosmeltRecipe((FurnaceRecipe) recipe);
                ShapelessRecipe philoRecipe = philosmeltRecipe.toRecipe();
                if (philoRecipe == null)
                    continue;

                Bukkit.removeRecipe(philoRecipe.getKey());
                Bukkit.addRecipe(philoRecipe);
                this.registeredPhilosmeltRecipes.put(philosmeltRecipe.getKey(), philosmeltRecipe);
            }

            this.plugin.logger.info(this.registeredPhilosmeltRecipes.size() + " Philosmelt Recipes been Registered.");
        }

        public @Nullable ItemStack getPhilosmeltResult(Recipe recipe) {
//        public @Nullable ItemStack getPhilosmeltResult(Recipe recipe, List<ItemStack> items) {
//            PhilosmeltRecipe philoRecipe = this.registeredPhilosmeltRecipes.get(((ShapelessRecipe) recipe).getKey());
//            if (philoRecipe == null)
//                return null;
//
//            ItemStack b = null;
//            RecipeChoice a = philoRecipe.getIngredient();
//            if (a instanceof RecipeChoice.MaterialChoice)
//                b = ((RecipeChoice.MaterialChoice) a).getItemStack();
//            else if (a instanceof RecipeChoice.ExactChoice)
//                b = ((RecipeChoice.ExactChoice) a).getItemStack();
//            if (b == null)
//                return null;
//            ItemStack fb = b;
//
//            Optional<ItemStack> fuelItemFind = items.stream()
//                .filter(item -> !isPhilostoneItem(item) && !fb.isSimilar(item))
//                .findFirst();
//            if (fuelItemFind.isEmpty())
//                return null;
//
//            ItemStack fuelItem = fuelItemFind.get();
//
//            plugin.logger.info(fuelItem + " " + philoRecipe.getBurnableFuels());
//            if (!philoRecipe.getBurnableFuels().contains(fuelItem))
//                return null;
//
////            for (ItemStack item : items) {
////                \
////            }

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
                emcBuilder.conversion(node.resultForward(), 1).addIngredient(node.origin()).end();

                if (!node.resultForward().equals(node.resultBackward()))
                    emcBuilder.conversion(node.resultBackward(), 1).addIngredient(node.origin()).end();
            }
        }
    }
}
