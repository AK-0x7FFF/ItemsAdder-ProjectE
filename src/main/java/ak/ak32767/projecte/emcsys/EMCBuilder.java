package ak.ak32767.projecte.emcsys;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.ProjectEException;
import ak.ak32767.projecte.data.ExactItemWrapper;
import ak.ak32767.projecte.data.IAItemWrapper;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class EMCBuilder {
    private static final boolean IAItemTracker = false;
    private static final String IA_FAKE_RECIPE_PREFIX = "zzzfake";

    private final ProjectE plugin;
    private final Object2LongLinkedOpenHashMap<Material> fixedValues;
    private final Object2ObjectOpenHashMap<Object, BigInteger> emcValues;
    private final ArrayList<ConversionBuilder<EMCBuilder>> conversions;


    public EMCBuilder(ProjectE plugin) {
        this.plugin = plugin;

        this.fixedValues = new Object2LongLinkedOpenHashMap<>();
        this.fixedValues.defaultReturnValue(0);
        this.conversions = new ArrayList<>();
        this.emcValues = new Object2ObjectOpenHashMap<>();
        this.emcValues.defaultReturnValue(BigInteger.valueOf(0));
    }

    public Object2ObjectOpenHashMap<Object, BigInteger> build(WorldTransmutationsBuilder worldTransmutationsBuilder) throws FileNotFoundException {
        this.fixedValues.clear();
        this.conversions.clear();
        this.emcValues.clear();

        buildByYAML();
        buildWorldTransmutations2Conversion(worldTransmutationsBuilder);
        buildRecipeConversion();

        this.plugin.logger.info(this.conversions.size() + " conversions been Registered.");
        calcEMC();

        this.conversions.clear();
        return emcValues;
    }

    public void calcEMC() {
        int changed = 1;
        short depth = 0;
        for (; changed > 0 && depth < Byte.MAX_VALUE; depth++) {
            changed = 0;

            for (ConversionBuilder<EMCBuilder> conversion : this.conversions) {
                Object target = conversion.getResult();
                boolean tracker = this.IAItemTracker && target instanceof IAItemWrapper;

                // 硬編碼跳過
                if (target instanceof Material && this.getFixedMaterialEmc((Material) target) > 0)
                    continue;

                BigInteger cost = BigInteger.ZERO;
                boolean calcable = true;

                if (tracker)
                    this.plugin.logger.info("[EMCBuilder:IAItemTracker] Calculating BGN: " + ((IAItemWrapper) target).getNamespacedID());

                // 遍歷原料
                for (var entry : conversion.getIngredients().object2LongEntrySet()) {
                    Set<Object> choices = entry.getKey();
                    long amount = entry.getLongValue();

                    // 佔位符處理
                    if (choices.contains(Material.AIR)) {
                        cost = cost.add(BigInteger.valueOf(amount));
                        if (tracker)
                            this.plugin.logger.info("[EMCBuilder:IAItemTracker]  - ADD_HARD: " + amount + " -> " + cost);
                        continue;
                    }

                    // 跳過賢者之石
                    if (choices.stream().anyMatch(
                            obj -> obj instanceof IAItemWrapper &&
                            ((IAItemWrapper) obj).getNamespacedID().equals("projecte:philosophers_stone")
                    ))
                        continue;

                    if (tracker)
                        this.plugin.logger.info("[EMCBuilder:IAItemTracker]  - CHOICES: " +
                            choices.stream()
                            .map(choice -> {
                                if (choice instanceof Material)
                                    return "MATERIAL:" + choice;

                                if (choice instanceof ExactItemWrapper)
                                    return "EXACT_ITEM:" + ((ExactItemWrapper) choice).getMaterial() + "->" + ((ExactItemWrapper) choice).getMeta().toString();

                                if (choice instanceof IAItemWrapper)
                                    return "IAITEM:" + ((IAItemWrapper) choice).getNamespacedID().toUpperCase();

                                return "NUL";
                            }).toList()
                        );

                    BigInteger minEmc = null;
                    for (Object item : choices) {
                        BigInteger curr = this.getEmc(item);

                        // 未迭代或無EMC
                        if (curr.equals(BigInteger.ZERO))
                            continue;

                        if (minEmc == null || curr.compareTo(minEmc) < 0)
                            minEmc = curr;
                    }

                    if (minEmc == null) {
                        calcable = false;
                        break;
                    }

                    minEmc = minEmc.multiply(BigInteger.valueOf(amount));
                    cost = cost.add(minEmc);
                    if (tracker)
                        this.plugin.logger.info("[EMCBuilder:IAItemTracker]  - ADD: +" + minEmc + " -> " + cost);
                }
                if (tracker)
                    this.plugin.logger.info("[EMCBuilder:IAItemTracker] Calculating END");

                if (!calcable || cost.compareTo(BigInteger.ZERO) <= 0)
                    continue;

                BigInteger itemEmc = cost.divide(BigInteger.valueOf(conversion.getAmount()));
                if (itemEmc.compareTo(BigInteger.ZERO) <= 0)
                    continue;

                BigInteger prevItemEmc = getCalcedItemEmc(target);
                if (!prevItemEmc.equals(BigInteger.ZERO) && itemEmc.compareTo(prevItemEmc) >= 0)
                    continue;

                this.emcValues.put(target, itemEmc);
                changed++;
            }
            this.plugin.logger.info("EMC Map Calculated, depth: " + depth + ", Changed: +" + changed);
        }

        if (depth == Byte.MAX_VALUE) {
//            this.plugin.logger.warning("OVERDEPTH!!!!!!!!!!!!!!!");
            throw new IllegalStateException("OVER DEPTH !!!!!!!!!!!!!!!");
        }
    }

    public long getFixedMaterialEmc(Material material) {
        return this.fixedValues.getLong(material);

    }

    public BigInteger getCalcedItemEmc(Object item) {
        return this.emcValues.get(item);
    }

    public BigInteger getEmc(Object item) {
        if (item instanceof Material) {
            long fixedValue = this.getFixedMaterialEmc((Material) item);
            if (fixedValue > 0)
                return BigInteger.valueOf(fixedValue);

            BigInteger value = this.getCalcedItemEmc(item);
            if (value.compareTo(BigInteger.ZERO) > 0) {
                return value;
            }
            return BigInteger.ZERO;
        }

        if (item instanceof ExactItemWrapper) {
            BigInteger value = this.getCalcedItemEmc(item);
            if (value.compareTo(BigInteger.ZERO) > 0)
                return value;

            Material material = ((ExactItemWrapper) item).getMaterial();
            value = this.getCalcedItemEmc(material);
            if (value.compareTo(BigInteger.ZERO) > 0)
                return value;
        } else if (item instanceof IAItemWrapper) {
            BigInteger value = this.getCalcedItemEmc(item);
            if (value.compareTo(BigInteger.ZERO) > 0)
                return value;
        }

        return BigInteger.ZERO;
    }

    private void buildRecipeConversion() {
        for (Iterator<Recipe> recipes = Bukkit.recipeIterator(); recipes.hasNext();) {
            Recipe recipe = recipes.next();

            // 跳過IA假配方
            if (((Keyed) recipe).getKey().getNamespace().startsWith(IA_FAKE_RECIPE_PREFIX))
                continue;

            ItemStack resultItem = recipe.getResult();
            Material resultMaterial = resultItem.getType();
            int resultAmount = resultItem.getAmount();

            if (resultMaterial.equals(Material.AIR))
                continue;

            ArrayList<RecipeChoice> choices = new ArrayList<>();
            switch (recipe) {
                case ShapelessRecipe    shapelessRecipe     -> choices.addAll(shapelessRecipe.getChoiceList());
                case ShapedRecipe       shapedRecipe        -> choices.addAll(shapedRecipe.getChoiceMap().values());
                case CookingRecipe<?>   cookingRecipe       -> choices.add(cookingRecipe.getInputChoice());
                case StonecuttingRecipe stonecuttingRecipe  -> choices.add(stonecuttingRecipe.getInputChoice());
                case SmithingTransformRecipe smithingRecipe -> {
                    choices.add(smithingRecipe.getBase());
                    choices.add(smithingRecipe.getAddition());
                    choices.add(smithingRecipe.getTemplate());
                }
                default -> {}
            }

            if (choices.isEmpty())
                continue;

            ConversionBuilder<EMCBuilder> conversion = this.conversion(resultItem, resultAmount);

            for (RecipeChoice choice : choices) {
                // 常規選物
                if (choice instanceof RecipeChoice.MaterialChoice) {
                    Set<Object> materials = new ObjectOpenHashSet<>(((RecipeChoice.MaterialChoice) choice).getChoices());
                    conversion.addIngredientsGroup(materials);

                // 精確選物
                } else if (choice instanceof RecipeChoice.ExactChoice) {
                    Set<Object> items = ((RecipeChoice.ExactChoice) choice).getChoices()
                            .stream().map(ExactItemWrapper::of)
                            .collect(Collectors.toSet());
                    conversion.addIngredientsGroup(items);
                }
            }

            conversion.end();
        }
    }

    private void buildByYAML() throws FileNotFoundException {
        InputStream fileStream = this.plugin.getResource("data/emc_data.yml");
        if  (fileStream == null)
            throw new FileNotFoundException("data/emc_data.yml file not found!");

        try (InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(reader);

            // this.fixedValue
            @NotNull List<Map<?, ?>> fixed = config.getMapList("fixed");
            buildFixedByYAML(fixed);

            // this.conversions
            List<Map<?, ?>> conversions = config.getMapList("conversion");
            buildConversionByYAML(conversions);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void buildFixedByYAML(@NotNull List<Map<?, ?>> fixedList) {
        for (Map<?, ?> fixed : fixedList) {
            String name = String.valueOf(fixed.get("name")).toUpperCase();
            String type = String.valueOf(fixed.get("type")).toUpperCase();
            int value   = ((Number) fixed.get("value")).intValue();

            if (type.equalsIgnoreCase("MATERIAL")) {
                Material material = Material.getMaterial(name);
                if (material == null)
                    throw new ProjectEException.YAMLMaterialNotFoundException();

                this.fixed(material, value);

            } else if (type.equalsIgnoreCase("TAG")) {
                Tag<Material> tag = Bukkit.getTag(
                    Tag.REGISTRY_ITEMS,
                    NamespacedKey.minecraft(name.toLowerCase()),
                    Material.class
                );

                if (tag == null) {
                    // UNSAFY, 但能用
                    try {
                        Field field = Tag.class.getField(name);
                        tag = (Tag<Material>) field.get(null);
                        tag.getValues().forEach(mat -> this.fixed(mat, value));
                    } catch (Exception ignored) {
                        throw new ProjectEException.YAMLTagNotFoundException();
                    }
                }

                this.fixed(tag, value);
            }
        }
    }

    private void buildConversionByYAML(List<Map<?, ?>> conversionList) {
        for (Map<?, ?> conversion : conversionList) {
            String name = String.valueOf(conversion.get("name")).toUpperCase();
            String type = String.valueOf(conversion.get("type")).toUpperCase();
            long amount = ((Number) conversion.get("amount")).longValue();
            List<Map<?, ?>> ingredients = (List<Map<?, ?>>) conversion.get("ingredient");

            ConversionBuilder<EMCBuilder> cb = null;
            if (type.equalsIgnoreCase("MATERIAL")) {
                Material material = Material.getMaterial(name);
                if (material == null)
                    throw new ProjectEException.YAMLMaterialNotFoundException();

                cb = this.conversion(material, amount);
            }

            if (cb == null)
                throw new RuntimeException();

            for (Map<?, ?> ingredient : ingredients) {
                 String ingrType = String.valueOf(ingredient.get("type")).toUpperCase();

                 if  (ingrType.equalsIgnoreCase("MATERIAL")) {
                     String ingrName = String.valueOf(ingredient.get("name")).toUpperCase();
                     long ingrAmount = ((Number) ingredient.get("amount")).longValue();

                     Material material = Material.getMaterial(ingrName);
                     if (material == null)
                         throw new ProjectEException.YAMLMaterialNotFoundException();

                     cb.addIngredient(material, ingrAmount);

                 } else if (ingrType.equalsIgnoreCase("ADD")) {
                     long ngrValue = ((Number) ingredient.get("value")).longValue();
                     cb.addValue(ngrValue);
                 }

                 cb.end();
            }




        }
    }

    private void buildWorldTransmutations2Conversion(WorldTransmutationsBuilder worldTransmutationsBuilder) {
        for (var node: worldTransmutationsBuilder.getRegistered()) {
            this.conversion(node.getResultForward()).addIngredient(node.getOrigin()).end();

            if (!node.getResultForward().equals(node.getResultBackward()))
                this.conversion(node.getResultBackward()).addIngredient(node.getOrigin()).end();
        }
    }

    public EMCBuilder fixed(Material material, long value) {
        this.fixedValues.put(material, value);
        return this;
    }

    public EMCBuilder fixed(Tag<Material> tag, int value) {
        for (Material material : tag.getValues()) {
            fixed(material, value);
        }
        return this;
    }

    public ConversionBuilder<EMCBuilder> conversion(Material materials) {
        return conversion(materials, 1);
    }

    public ConversionBuilder<EMCBuilder> conversion(Material materials, long amount) {
        ConversionBuilder<EMCBuilder> builder = new ConversionBuilder<>(this, materials, amount);
        this.conversions.add(builder);
        return builder;
    }

    public ConversionBuilder<EMCBuilder> conversion(ItemStack item, long amount) {
        Object itemWrapped = ExactItemWrapper.of(item);
        ConversionBuilder<EMCBuilder> builder = new ConversionBuilder<>(this, itemWrapped, amount);
        this.conversions.add(builder);
        return builder;
    }
}
