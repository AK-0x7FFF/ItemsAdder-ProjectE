package ak.ak32767.projecte.emcsys;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.ProjectEException;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.utils.YAMLLoader;
import dev.lone.itemsadder.api.ItemsAdder;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class EMCBuilder {
    private static final boolean IAItemTracker = false;
    private static final String IA_FAKE_RECIPE_PREFIX = "zzzfake";

    private final ProjectE plugin;
    private final Object2LongLinkedOpenHashMap<ItemWrapper.TransmutableItem> fixedValues;
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
        this.plugin.logger.info(this.fixedValues.size() + " fixedValues been Set.");
        this.plugin.logger.info(this.conversions.size() + " conversions been Registered.");

        calcEMC();
        int emcedItem = this.fixedValues.size() + this.emcValues.size();
        long allRegisteredItem = Arrays.stream(Material.values()).filter(Material::isItem).filter(mat -> !mat.isLegacy()).count() + ItemsAdder.getAllItems().size();
        this.plugin.logger.info(this.emcValues.size() + " EMC been Calculated.");
        this.plugin.logger.info("EMCed Item: " + emcedItem);
        this.plugin.logger.info("unEMC Item: " + (allRegisteredItem - emcedItem));


        this.conversions.clear();
        return emcValues;
    }

    public void calcEMC() {
        this.plugin.logger.info("EMCs Calculate BGN");

        int changed = 1;
        short depth = 0;
        for (; changed > 0 && depth < Byte.MAX_VALUE; depth++) {
            changed = 0;

            for (ConversionBuilder<EMCBuilder> conversion : this.conversions) {
                Object target = conversion.getResult();
                boolean tracker = this.IAItemTracker && target instanceof ItemWrapper.IAItem;

                // 硬編碼跳過
                if (target instanceof ItemWrapper.MaterialItem && this.getFixedMaterialEmc((ItemWrapper.MaterialItem) target) > 0)
                    continue;

                BigInteger cost = BigInteger.ZERO;
                boolean calcable = true;

                if (tracker)
                    this.plugin.logger.info("[EMCBuilder:IAItemTracker] Calculating BGN: " + ((ItemWrapper.IAItem) target).namespacedID());

                // 遍歷原料
                for (var entry : conversion.getIngredients().object2LongEntrySet()) {
                    Set<ItemWrapper.TransmutableItem> choices = entry.getKey();
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
                            obj -> obj instanceof ItemWrapper.IAItem &&
                            ((ItemWrapper.IAItem) obj).namespacedID().equals("projecte:philosophers_stone")
                    ))
                        continue;

                    if (tracker)
                        this.plugin.logger.info("[EMCBuilder:IAItemTracker]  - CHOICES: " +
                            choices.stream()
                            .map(choice -> {
                                if (choice instanceof ItemWrapper.MaterialItem)
                                    return "MATERIAL:" + choice;

                                if (choice instanceof ItemWrapper.ExactItem)
                                    return "EXACT_ITEM:" + ((ItemWrapper.ExactItem) choice).material() + "->" + ((ItemWrapper.ExactItem) choice).meta().toString();

                                if (choice instanceof ItemWrapper.IAItem)
                                    return "IAITEM:" + ((ItemWrapper.IAItem) choice).namespacedID().toUpperCase();

                                return "NUL";
                            }).toList()
                        );

                    BigInteger minEmc = null;
                    for (ItemWrapper.TransmutableItem item : choices) {
                        BigInteger curr = this.getEMCRaw(item);

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
            this.plugin.logger.info("EMCs Calculating, depth: " + depth + ", Changed: +" + changed);
        }

        if (depth == Byte.MAX_VALUE) {
//            this.plugin.logger.warning("OVERDEPTH!!!!!!!!!!!!!!!");
            throw new IllegalStateException("OVER DEPTH !!!!!!!!!!!!!!!");
        }

        this.plugin.logger.info("EMCs Calculate END");
    }

    public long getFixedMaterialEmc(ItemWrapper.TransmutableItem item) {
        return this.fixedValues.getLong(item);
    }

    public BigInteger getCalcedItemEmc(Object item) {
        return this.emcValues.get(item);
    }

    public BigInteger getEMCRaw(ItemWrapper.TransmutableItem item) {
        long fixedValue = this.getFixedMaterialEmc(item);
        if (fixedValue > 0)
            return BigInteger.valueOf(fixedValue);

        BigInteger value = this.getCalcedItemEmc(item);
        if (value.compareTo(BigInteger.ZERO) > 0)
            return value;

        if (item instanceof ItemWrapper.ExactItem) {
            Material material = ((ItemWrapper.ExactItem) item).material();
            value = this.getCalcedItemEmc(material);
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
                    Set<ItemWrapper.TransmutableItem> items = new ObjectOpenHashSet<>(
                        ((RecipeChoice.MaterialChoice) choice).getChoices().stream()
                        .map(ItemWrapper.MaterialItem::new)
                        .collect(Collectors.toSet())
                    );
                    conversion.addIngredientsGroup(items);

                // 精確選物
                } else if (choice instanceof RecipeChoice.ExactChoice) {
                    Set<ItemWrapper.TransmutableItem> items = ((RecipeChoice.ExactChoice) choice).getChoices()
                            .stream().map(ItemWrapper::of)
                            .collect(Collectors.toSet());
                    conversion.addIngredientsGroup(items);
                }
            }

            conversion.end();
        }
    }



    private void buildByYAML() throws FileNotFoundException {
        FileConfiguration config = YAMLLoader.load(this.plugin, "data/emc_data.yml");

        // this.fixedValue
        @NotNull List<Map<?, ?>> fixed = config.getMapList("fixed");
        buildFixedByYAML(fixed);

        // this.conversions
        List<Map<?, ?>> conversions = config.getMapList("conversion");
        buildConversionByYAML(conversions);
    }

    private void buildFixedByYAML(@NotNull List<Map<?, ?>> fixeds) {
        for (Map<?, ?> entry : fixeds) {
            int value = ((Number) entry.get("value")).intValue();
             List<ItemWrapper.TransmutableItem> items = YAMLLoader.ItemYAMLWrapper.of(entry);

             for (ItemWrapper.TransmutableItem item : items)
                 this.fixed(item, value);
        }
    }

    private void buildConversionByYAML(List<Map<?, ?>> conversions) {
        for (Map<?, ?> conversion : conversions) {
            long resultAmount = ((Number) conversion.get("amount")).longValue();
            List<Map<?, ?>> ingredients = (List<Map<?, ?>>) conversion.get("ingredient");

            List<ItemWrapper.TransmutableItem> resultItems = YAMLLoader.ItemYAMLWrapper.of(conversion);
            if (resultItems.isEmpty())
                throw new IllegalStateException();


            List<ConversionBuilder<EMCBuilder>> cbs = resultItems.stream().map(item -> this.conversion(item, resultAmount)).toList();
            for (Map<?, ?> ingredient : ingredients) {
                 String type = String.valueOf(ingredient.get("type")).toUpperCase();

                 if (type.equalsIgnoreCase("ADD")) {
                     long value = ((Number) ingredient.get("value")).longValue();
                     cbs.forEach(cb -> cb.addValue(value));
                 } else {
                     long amount = ((Number) ingredient.get("amount")).longValue();
                     List<ItemWrapper.TransmutableItem> items = YAMLLoader.ItemYAMLWrapper.of(ingredient);
                     if (items.isEmpty())
                         throw new IllegalStateException();

                     cbs.forEach(cb -> cb.addIngredient(items.getFirst(), amount));
                 }

                cbs.forEach(cb -> cb.end());
            }
        }
    }

    private void buildWorldTransmutations2Conversion(WorldTransmutationsBuilder worldTransmutationsBuilder) {
        for (var node: worldTransmutationsBuilder.getRegistered()) {
            this.conversion(node.resultForward()).addIngredient(node.origin()).end();

            if (!node.resultForward().equals(node.resultBackward()))
                this.conversion(node.resultBackward()).addIngredient(node.origin()).end();
        }
    }

    public EMCBuilder fixed(ItemWrapper.TransmutableItem item, long value) {
        this.fixedValues.put(item, value);
        return this;
    }

    public ConversionBuilder<EMCBuilder> conversion(ItemWrapper.TransmutableItem materials) {
        return conversion(materials, 1);
    }

    public ConversionBuilder<EMCBuilder> conversion(ItemWrapper.TransmutableItem item, long amount) {
        ConversionBuilder<EMCBuilder> builder = new ConversionBuilder<>(this, item, amount);
        this.conversions.add(builder);
        return builder;
    }

    public ConversionBuilder<EMCBuilder> conversion(ItemStack item, long amount) {
        ItemWrapper.TransmutableItem itemWrapped = ItemWrapper.of(item);
        ConversionBuilder<EMCBuilder> builder = new ConversionBuilder<>(this, itemWrapped, amount);
        this.conversions.add(builder);
        return builder;
    }
}
