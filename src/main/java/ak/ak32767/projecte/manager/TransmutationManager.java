package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.emcsys.PhiloCraftTransmutationBuilder;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Set;

public class TransmutationManager {
    private final WorldTransmutation worldTransmutation;
    private final PhiloCraftTransmutation philoCraftTransmutation;

    public TransmutationManager(ProjectE plugin, PhiloRecipeManager philoRecipeManager) {
        this.worldTransmutation = new WorldTransmutation(plugin);
        this.philoCraftTransmutation = new PhiloCraftTransmutation(plugin, philoRecipeManager);
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
        private final ProjectE plugin;
        private final PhiloRecipeManager manager;
        private final PhiloCraftTransmutationBuilder builder;

        private PhiloCraftTransmutation(ProjectE plugin, PhiloRecipeManager manager) {
            this.plugin = plugin;
            this.manager = manager;
            this.builder = new PhiloCraftTransmutationBuilder(plugin);
        }

        @Override
        public boolean build() {
            return this.builder.build();
        }

        @Override
        public void conversionsRegister(EMCBuilder emcBuilder) {
            for (var data : this.builder.getConversions()) {
                emcBuilder
                .register(data.itemA(), data.amountA()).addIngredient(data.itemB(),  data.amountB()).end()
                .register(data.itemB(), data.amountB()).addIngredient(data.itemA(),  data.amountA()).end();
            }
        }

        public Set<Recipe> toRecipes() {
            final Material PHILOSTONE = CustomStack.getInstance("projecte:philosophers_stone").getItemStack().getType();

            Set<Recipe> recipes = new ObjectLinkedOpenHashSet<>(this.builder.getConversions().size() << 1);

            for (var data : this.builder.getConversions()) {
                ItemStack itemA = data.itemA().item();
                ItemStack itemB = data.itemB().item();

                String itemAName = PlainTextComponentSerializer.plainText().serialize(itemA.displayName())
                        .toLowerCase().replaceAll("[^a-z0-9/._-]", "");
                String itemBName = PlainTextComponentSerializer.plainText().serialize(itemB.displayName())
                        .toLowerCase().replaceAll("[^a-z0-9/._-]", "");

                ItemStack result;
                String recipeKey;
                PhiloRecipeManager.PhiloRecipe recipe;

                result = itemB.clone();
                result.setAmount((int) data.amountB());
                recipeKey = itemAName + "2" + itemBName;

                recipe = new PhiloRecipeManager.PhiloRecipe(result, itemA, (int) data.amountA());
                recipes.add(this.manager.a(recipeKey, recipe));
                this.manager.register(recipeKey, result, itemA, (int) data.amountA());

                result = itemA.clone();
                result.setAmount((int) data.amountA());
                recipeKey = itemBName + "2" + itemAName;

                recipe = new PhiloRecipeManager.PhiloRecipe(result, itemB, (int) data.amountB());
                recipes.add(this.manager.a(recipeKey, recipe));
                this.manager.register(recipeKey, result, itemB, (int) data.amountB());

//                // A -> B
//                {
//                    ItemStack result = itemB.clone();
//                    result.setAmount((int) data.amountB());
//
//                    String recipeKey = itemAName + "2" + itemBName;
//                    ShapelessRecipe recipe = new ShapelessRecipe(
//                            new NamespacedKey(PhiloRecipeManager.PHILO_RECIPE_NAMESPACE, recipeKey), result
//                    );
//                    recipe.addIngredient(PHILOSTONE);
//
//                    RecipeChoice choice;
//                    if (data.itemA() instanceof ItemWrapper.IAItem)  choice = new RecipeChoice.MaterialChoice(data.itemA().material());
//                    else choice = new RecipeChoice.ExactChoice(itemA);
//                    for (int i = 0; i < data.amountA(); ++i) {
//                        recipe.addIngredient(choice);
//                    }
//
//                    recipes.add(recipe);
//                    this.manager.register(recipeKey, result, itemA, (int) data.amountA());
//                }
            }

            return recipes;
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
