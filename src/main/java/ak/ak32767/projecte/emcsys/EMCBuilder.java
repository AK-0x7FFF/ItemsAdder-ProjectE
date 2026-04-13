package ak.ak32767.projecte.emcsys;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.ProjectEException;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.event.EMCPreCalculateEvent;
import ak.ak32767.projecte.utils.YAMLLoader;
import dev.lone.itemsadder.api.ItemsAdder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static ak.ak32767.projecte.emcsys.ItemConversionBuilder.BLANK;

public class EMCBuilder {
    private static final boolean IAItemTracker = false;
    private static final String IA_FAKE_RECIPE_PREFIX = "zzzfake";

    private final ProjectE plugin;
//    private final EMCManager manager;
    private final Object2ObjectOpenHashMap<ItemWrapper.TransmutableItem, BigDecimal> fixedValues;
    private final Object2ObjectOpenHashMap<ItemWrapper.TransmutableItem, BigDecimal> emcValues;
    private final ArrayList<ItemConversionBuilder> conversions;


    public EMCBuilder(ProjectE plugin) {
        this.plugin = plugin;
//        this.manager = manager;
        this.conversions = new ArrayList<>();

        this.fixedValues = new Object2ObjectOpenHashMap<>();
        this.fixedValues.defaultReturnValue(BigDecimal.ZERO);

        this.emcValues = new Object2ObjectOpenHashMap<>();
        this.emcValues.defaultReturnValue(BigDecimal.ZERO);
    }

    public Object2ObjectOpenHashMap<ItemWrapper.TransmutableItem, BigInteger> build() {
        // 從 .yml 構建
        try {
            FileConfiguration config = YAMLLoader.load(this.plugin, "data/emc_data.yml");
            {
                List<Map<?, ?>> fixed = config.getMapList("fixed");
                fixedByYAML(fixed);

                List<Map<?, ?>> conversions = config.getMapList("conversion");
                conversionByYAML(conversions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 從 合成表 構建
        for (Iterator<Recipe> recipes = Bukkit.recipeIterator(); recipes.hasNext(); ) {
            Recipe recipe = recipes.next();
            addConversionByRecipe(recipe);
        }

        // 從 事件 構建
        EMCPreCalculateEvent preCalcEvent = new EMCPreCalculateEvent(this);
        Bukkit.getPluginManager().callEvent(preCalcEvent);

        this.plugin.logger.info(this.fixedValues.size() + " fixedValues been Set.");
        this.plugin.logger.info(this.conversions.size() + " conversions been Registered.");

        calculateEMC(); {
            int emcedItem = this.fixedValues.size() + this.emcValues.size();
            long allRegisteredItem = Arrays.stream(Material.values()).filter(Material::isItem).filter(mat -> !mat.isLegacy()).count() + ItemsAdder.getAllItems().size();
            this.plugin.logger.info(this.emcValues.size() + " EMC been Calculated.");
            this.plugin.logger.info("EMCed Item: " + emcedItem + ", unEMC Item: " + (allRegisteredItem - emcedItem));
        }

        this.conversions.clear();
        this.emcValues.putAll(this.fixedValues);
        Object2ObjectOpenHashMap<ItemWrapper.TransmutableItem, BigInteger> emcIntValues = new Object2ObjectOpenHashMap<>(this.emcValues.size());
        this.emcValues.object2ObjectEntrySet().fastForEach(
            entry -> emcIntValues.put(entry.getKey(), entry.getValue().toBigInteger())
        );
        return emcIntValues;
    }

    public void calculateEMC() {
        this.plugin.logger.info("EMCs Calculate BGN");

        int changed = 1;
        short depth = 0;
        for (; changed > 0 && depth < Byte.MAX_VALUE; depth++) {
            changed = 0;

            for (ItemConversionBuilder conversion : this.conversions) {
                ItemWrapper.TransmutableItem target = conversion.getResult();
                boolean iaTracker = IAItemTracker && target instanceof ItemWrapper.IAItem;

                // 硬編碼跳過
                if (target instanceof ItemWrapper.MaterialItem && this.getFixedItemEmc(target).compareTo(BigDecimal.ZERO) > 0)
                    continue;

                BigDecimal cost = BigDecimal.ZERO;
                boolean calcable = true;

                if (iaTracker)
                    this.plugin.logger.info("[EMCBuilder:IAItemTracker] Calculating BGN: " + ((ItemWrapper.IAItem) target).namespacedID());

                // 遍歷原料
                for (var entry : conversion.getIngredients().object2LongEntrySet()) {
                    Set<ItemWrapper.TransmutableItem> choices = entry.getKey();
                    long amount = entry.getLongValue();

                    // 佔位符處理
                    if (choices == BLANK) {
                        cost = cost.add(BigDecimal.valueOf(amount));
                        if (iaTracker)
                            this.plugin.logger.info("[EMCBuilder:IAItemTracker]  - ADD_HARD: " + amount + " -> " + cost);
                        continue;
                    }

                    // 跳過賢者之石
                    if (choices.stream().anyMatch(
                            obj -> obj instanceof ItemWrapper.IAItem &&
                                    ((ItemWrapper.IAItem) obj).namespacedID().equals("projecte:philosophers_stone")
                    ))
                        continue;

                    if (iaTracker)
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

                    BigDecimal minEmc = null;
                    for (ItemWrapper.TransmutableItem item : choices) {
                        BigDecimal curr = this.getEMCRaw(item);

                        // 未迭代或無EMC
                        if (curr.compareTo(BigDecimal.ZERO) <= 0)
                            continue;

                        // 合成歸還物品EMC減除
                        if (item.material().isItem()) {
                            Material craftRemainMaterial = item.material().getCraftingRemainingItem();
                            if (craftRemainMaterial != null) {
                                BigDecimal remainEMC = getEMCRaw(new ItemWrapper.MaterialItem(craftRemainMaterial));
                                if (remainEMC.compareTo(BigDecimal.ZERO) > 0)
                                    curr = curr.subtract(remainEMC);
                            }
                        }

                        if (minEmc == null || curr.compareTo(minEmc) < 0)
                            minEmc = curr;
                    }

                    if (minEmc == null) {
                        calcable = false;
                        break;
                    }

                    minEmc = minEmc.multiply(BigDecimal.valueOf(amount));
                    cost = cost.add(minEmc);
                    if (iaTracker)
                        this.plugin.logger.info("[EMCBuilder:IAItemTracker]  - ADD: +" + minEmc + " -> " + cost);
                }
                if (iaTracker)
                    this.plugin.logger.info("[EMCBuilder:IAItemTracker] Calculating END");

                if (!calcable || cost.compareTo(BigDecimal.ZERO) <= 0)
                    continue;

                BigDecimal itemEmc = cost.divide(BigDecimal.valueOf(conversion.getAmount()), 4, RoundingMode.HALF_UP);
                if (itemEmc.compareTo(BigDecimal.ZERO) <= 0)
                    continue;

                BigDecimal prevItemEmc = getCalcedItemEmc(target);
                if (prevItemEmc.compareTo(BigDecimal.ZERO) > 0 && itemEmc.compareTo(prevItemEmc) >= 0)
                    continue;

                this.emcValues.put(target, itemEmc);
                changed++;
            }
            this.plugin.logger.info("   Depth: " + depth + ", Changed: +" + changed);
        }

        if (depth == Byte.MAX_VALUE) {
//            this.plugin.logger.warning("OVERDEPTH!!!!!!!!!!!!!!!");
            throw new IllegalStateException("OVER DEPTH !!!!!!!!!!!!!!!");
        }

        this.plugin.logger.info("EMCs Calculate END");
        }

    private BigDecimal getFixedItemEmc(ItemWrapper.TransmutableItem item) {
        return this.fixedValues.get(item);
    }

    private BigDecimal getCalcedItemEmc(ItemWrapper.TransmutableItem item) {
        return this.emcValues.get(item);
    }

    private BigDecimal getEMCRaw(ItemWrapper.TransmutableItem item) {
        BigDecimal fixedValue = this.getFixedItemEmc(item);
        if (fixedValue.compareTo(BigDecimal.ZERO) > 0)
            return fixedValue;

        BigDecimal value = this.getCalcedItemEmc(item);
        if (value.compareTo(BigDecimal.ZERO) > 0)
            return value;

        if (item instanceof ItemWrapper.ExactItem) {
            ItemWrapper.MaterialItem material = ItemWrapper.toMaterialItem(item);
            value = this.getCalcedItemEmc(material);
            if (value.compareTo(BigDecimal.ZERO) > 0)
                return value;
        }

        return BigDecimal.ZERO;
    }

    public boolean addConversionByRecipe(Recipe recipe) {
        // 跳過IA假配方
        if (((Keyed) recipe).getKey().getNamespace().startsWith(IA_FAKE_RECIPE_PREFIX))
            return false;

        ItemStack resultItem = recipe.getResult();
        Material resultMaterial = resultItem.getType();
        int resultAmount = resultItem.getAmount();

        if (resultMaterial.equals(Material.AIR))
            return false;

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
            return false;

        ItemConversionBuilder conversion = this.register(resultItem, resultAmount);

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
        return true;
    }

    private void fixedByYAML(@NotNull List<Map<?, ?>> fixeds) throws ProjectEException.YAMLKeyOrValueErrorException {
        for (Map<?, ?> entry : fixeds) {
            long value = ((Number) entry.get("value")).longValue();
            List<ItemWrapper.TransmutableItem> items = YAMLLoader.ItemYAMLWrapper.of(entry);

            for (ItemWrapper.TransmutableItem item : items)
                this.fixed(item, BigDecimal.valueOf(value));
        }
    }

    private void conversionByYAML(List<Map<?, ?>> conversions) throws ProjectEException.YAMLKeyOrValueErrorException {
        for (Map<?, ?> conversion : conversions) {
            long resultAmount = ((Number) conversion.get("amount")).longValue();
            List<Map<?, ?>> ingredients = (List<Map<?, ?>>) conversion.get("ingredient");

            List<ItemWrapper.TransmutableItem> resultItems = YAMLLoader.ItemYAMLWrapper.of(conversion);
            if (resultItems.isEmpty())
                throw new ProjectEException.YAMLKeyOrValueErrorException();


            List<ItemConversionBuilder> cbs = resultItems.stream().map(item -> this.register(item, resultAmount)).toList();
            for (Map<?, ?> ingredient : ingredients) {
                String type = String.valueOf(ingredient.get("type")).toUpperCase();

                if (type.equalsIgnoreCase("ADD")) {
                    long value = ((Number) ingredient.get("value")).longValue();
                    cbs.forEach(cb -> cb.addValue(value));
                } else {
                    long amount = ((Number) ingredient.get("amount")).longValue();
                    List<ItemWrapper.TransmutableItem> items = YAMLLoader.ItemYAMLWrapper.of(ingredient);
                    if (items.isEmpty())
                        throw new ProjectEException.YAMLKeyOrValueErrorException();

                    cbs.forEach(cb -> cb.addIngredient(items.getFirst(), amount));
                }

                cbs.forEach(ItemConversionBuilder::end);
            }
        }
    }

    public EMCBuilder fixed(ItemWrapper.TransmutableItem item, BigDecimal value) {
        this.fixedValues.put(item, value);
        return this;
    }

    public ItemConversionBuilder register(ItemStack item, long amount) {
        ItemWrapper.TransmutableItem itemWrapped = ItemWrapper.of(item);
        return this.register(itemWrapped, amount);
    }

    public ItemConversionBuilder register(ItemWrapper.TransmutableItem item, long amount) {
        ItemConversionBuilder builder = new ItemConversionBuilder(this, item, amount);
        this.conversions.add(builder);
        return builder;
    }

}
