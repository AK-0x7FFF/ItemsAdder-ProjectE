package ak.ak32767.projecte.emcsys;

import ak.ak32767.projecte.ProjectE;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.*;
import org.bukkit.inventory.*;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class EMCBuilder {
    private final ProjectE plugin;

    private final Object2LongLinkedOpenHashMap<Material> fixedValues;
    private final ArrayList<ConversionBuilder<EMCBuilder>> conversions;
    private final Object2ObjectOpenHashMap<Material, BigInteger> emcValues;


    public EMCBuilder(ProjectE plugin) {
        this.plugin = plugin;

        this.fixedValues = new Object2LongLinkedOpenHashMap<>();
        this.fixedValues.defaultReturnValue(0);
        this.conversions = new ArrayList<>();
        this.emcValues = new Object2ObjectOpenHashMap<>();
        this.emcValues.defaultReturnValue(BigInteger.ZERO);
    }

    public void build(WorldTransmutationsBuilder worldTransmutationsBuilder) {
        buildHardcodedFixed();
        buildHardcodedConversion(worldTransmutationsBuilder);
        buildRecipe();
        this.plugin.logger.info(this.conversions.size() + " conversions been Registered.");

        calcEMC();
    }

    public void calcEMC() {
        int changed = 1;
        short depth = 0;
        for (; changed > 0 && depth < Byte.MAX_VALUE; depth++) {
            changed = 0;

            for (ConversionBuilder<EMCBuilder> conversion : this.conversions) {
                Material targetMaterial = conversion.getMaterial();
                // 硬編碼跳過
                if (this.getFixedItemEmc(targetMaterial) > 0)
                    continue;

                BigInteger cost = BigInteger.ZERO;
                boolean calcable = true;

                for (var entry : conversion.getIngredients().object2LongEntrySet()) {
                    Set<Material> choices = entry.getKey();
                    long amount = entry.getLongValue();

                    // 佔位符處理
                    if (choices.contains(Material.AIR)) {
                        cost = cost.add(BigInteger.valueOf(amount));
                        continue;
                    }

                    BigInteger minEmc = null;
                    for (var material : choices) {
                        BigInteger curr = this.getItemEmc(material);

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

                    cost = cost.add(minEmc.multiply(BigInteger.valueOf(amount)));
                }

                if (!calcable || cost.compareTo(BigInteger.ZERO) <= 0)
                    continue;

                BigInteger itemEmc = cost.divide(BigInteger.valueOf(conversion.getAmount()));
                if (itemEmc.compareTo(BigInteger.ZERO) <= 0)
                    continue;

                BigInteger prevItemEmc = getCalcedItemEmc(targetMaterial);
                if (!prevItemEmc.equals(BigInteger.ZERO) && itemEmc.compareTo(prevItemEmc) >= 0)
                    continue;

                this.emcValues.put(targetMaterial, itemEmc);
                changed++;
            }
            this.plugin.logger.info("EMC Map Calculated, depth: " + depth + ", Changed: +" + changed);
        }

        if (depth == Byte.MAX_VALUE) {
            this.plugin.logger.warning("OVERDEPTH!!!!!!!!!!!!!!!");
        }
    }

    public long getFixedItemEmc(Material material) {
        return this.fixedValues.getLong(material);

    }

    public BigInteger getCalcedItemEmc(Material material) {
        return this.emcValues.get(material);
    }

    public BigInteger getItemEmc(Material material) {
        long fixedValue = this.getFixedItemEmc(material);
        if (fixedValue > 0) {
            return BigInteger.valueOf(fixedValue);
        }

        BigInteger value = this.getCalcedItemEmc(material);
        if (value.compareTo(BigInteger.ZERO) > 0) {
            return value;
        }

        return BigInteger.ZERO;
    }

    public void buildRecipe() {
        for (Iterator<Recipe> recipes = Bukkit.recipeIterator(); recipes.hasNext();) {
            Recipe recipe = recipes.next();

            ItemStack result = recipe.getResult();
            Material resultMaterial = result.getType();
            int resultAmount = result.getAmount();

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
//                default -> throw new RuntimeException("Invalid recipe type: " + recipe.getClass());
                default -> {}
            }

            if (choices.isEmpty())
                continue;

            ConversionBuilder<EMCBuilder> conversion = this.conversion(resultMaterial, resultAmount);

            for (RecipeChoice choice : choices) {
                if (choice instanceof RecipeChoice.MaterialChoice) {
                    Set<Material> materials = new ObjectOpenHashSet<>(((RecipeChoice.MaterialChoice) choice).getChoices());
                    conversion.addIngredientsGroup(materials);
                } else if (choice instanceof RecipeChoice.ExactChoice) {
                    Set<Material> materials = ((RecipeChoice.ExactChoice) choice).getChoices().stream()
                            .map(ItemStack::getType)
                            .filter(material -> material != Material.AIR)
                            .collect(Collectors.toSet());
                    conversion.addIngredientsGroup(materials);
                }
            }

            conversion.end();
        }
    }

    public void buildHardcodedFixed() {
        this
        // FROM PROJECTE: https://github.com/sinkillerj/ProjectE/blob/15d4ce65bd06eb4222709b984255fbf5080e78bc/src/datagen/java/moze_intel/projecte/common/PECustomConversionProvider.java
        .fixed(Material.IRON_INGOT, 256)
        .fixed(Material.AMETHYST_SHARD, 32)

        .fixed(Material.COBBLESTONE, 1)
        .fixed(Material.GRANITE, 16)
        .fixed(Material.DIORITE, 16)
        .fixed(Material.ANDESITE, 16)
        .fixed(Material.POINTED_DRIPSTONE, 16)
        .fixed(Material.END_STONE, 1)
        .fixed(Material.NETHERRACK, 1)
        .fixed(Material.BASALT, 4)
        .fixed(Material.BLACKSTONE, 4)
        .fixed(Material.COBBLED_DEEPSLATE, 2)
        .fixed(Material.TUFF, 4)
        .fixed(Material.CALCITE, 32)
        .fixed(Material.DIRT, 1)
        .fixed(Material.SAND, 1)
        .fixed(Material.RED_SAND, 1)
        .fixed(Material.SNOW, 1)
        .fixed(Material.ICE, 1)
        .fixed(Material.DEAD_BUSH, 1)
        .fixed(Material.GRAVEL, 4)
        .fixed(Material.CACTUS, 8)
        .fixed(Material.VINE, 8)
        .fixed(Material.MOSS_BLOCK, 12)
        .fixed(Material.COBWEB, 12)
        .fixed(Material.PINK_PETALS, 4)
        .fixed(Material.LILY_PAD, 16)
        .fixed(Material.SMALL_DRIPLEAF, 24)
        .fixed(Material.BIG_DRIPLEAF, 32)
        .fixed(Tag.FLOWERS, 32)
        .fixed(Tag.SMALL_FLOWERS, 16)
        .fixed(Material.RED_MUSHROOM, 32)
        .fixed(Material.BROWN_MUSHROOM, 32)
        .fixed(Material.SUGAR_CANE, 32)
        .fixed(Material.BAMBOO, 32)
        .fixed(Material.SOUL_SAND, 49)
        .fixed(Material.OBSIDIAN, 64)
        .fixed(Material.CRYING_OBSIDIAN, 768)
        .fixed(Material.SPONGE, 128)
        .fixed(Material.SHORT_GRASS, 1)
        .fixed(Material.SEAGRASS, 1)
        .fixed(Material.KELP, 1)
        .fixed(Material.SEA_PICKLE, 16)
        .fixed(Material.TALL_GRASS, 1)
        .fixed(Material.FERN, 1)
        .fixed(Material.LARGE_FERN, 1)
        .fixed(Material.MAGMA_BLOCK, 128)
        .fixed(Material.NETHER_SPROUTS, 1)
        .fixed(Material.CRIMSON_ROOTS, 1)
        .fixed(Material.WARPED_ROOTS, 1)
        .fixed(Material.WEEPING_VINES, 8)
        .fixed(Material.TWISTING_VINES, 8)
        .fixed(Material.GLOW_LICHEN, 8)
        .fixed(Material.CRIMSON_FUNGUS, 32)
        .fixed(Material.WARPED_FUNGUS, 32)
        .fixed(Material.SPORE_BLOSSOM, 64)
        .fixed(Material.TUBE_CORAL_BLOCK, 64)
        .fixed(Material.BRAIN_CORAL_BLOCK, 64)
        .fixed(Material.BUBBLE_CORAL_BLOCK, 64)
        .fixed(Material.FIRE_CORAL_BLOCK, 64)
        .fixed(Material.HORN_CORAL_BLOCK, 64)
        .fixed(Material.DEAD_TUBE_CORAL_BLOCK, 4)
        .fixed(Material.DEAD_BRAIN_CORAL_BLOCK, 4)
        .fixed(Material.DEAD_BUBBLE_CORAL_BLOCK, 4)
        .fixed(Material.DEAD_FIRE_CORAL_BLOCK, 4)
        .fixed(Material.DEAD_HORN_CORAL_BLOCK, 4)
        .fixed(Material.TUBE_CORAL_FAN, 16)
        .fixed(Material.BRAIN_CORAL_FAN, 16)
        .fixed(Material.BUBBLE_CORAL_FAN, 16)
        .fixed(Material.FIRE_CORAL_FAN, 16)
        .fixed(Material.HORN_CORAL_FAN, 16)
        .fixed(Material.DEAD_TUBE_CORAL_FAN, 1)
        .fixed(Material.DEAD_BRAIN_CORAL_FAN, 1)
        .fixed(Material.DEAD_BUBBLE_CORAL_FAN, 1)
        .fixed(Material.DEAD_FIRE_CORAL_FAN, 1)
        .fixed(Material.DEAD_HORN_CORAL_FAN, 1)
        .fixed(Material.TUBE_CORAL, 16)
        .fixed(Material.BRAIN_CORAL, 16)
        .fixed(Material.BUBBLE_CORAL, 16)
        .fixed(Material.FIRE_CORAL, 16)
        .fixed(Material.HORN_CORAL, 16)
        .fixed(Material.DEAD_TUBE_CORAL, 1)
        .fixed(Material.DEAD_BRAIN_CORAL, 1)
        .fixed(Material.DEAD_BUBBLE_CORAL, 1)
        .fixed(Material.DEAD_FIRE_CORAL, 1)
        .fixed(Material.DEAD_HORN_CORAL, 1)
        .fixed(Material.CHORUS_PLANT, 64)
        .fixed(Material.CHORUS_FLOWER, 96)
        .fixed(Material.CHORUS_FRUIT, 192)
        .fixed(Material.SCULK_VEIN, 4)
        .fixed(Material.SCULK_CATALYST, 8_040)

        .fixed(Material.WHEAT_SEEDS, 16)
        .fixed(Material.PUMPKIN_SEEDS, 16)
        .fixed(Material.MELON_SEEDS, 16)
        .fixed(Material.BEETROOT_SEEDS, 16)
        .fixed(Material.TORCHFLOWER_SEEDS, 16)
        .fixed(Material.PITCHER_POD, 16)

        .fixed(Material.MELON_SLICE, 16)
        .fixed(Material.SWEET_BERRIES, 16)
        .fixed(Material.GLOW_BERRIES, 16)
        .fixed(Material.WHEAT, 24)
        .fixed(Material.NETHER_WART, 24)
        .fixed(Material.APPLE, 128)
        .fixed(Material.PUMPKIN, 144)
        .fixed(Material.HONEY_BOTTLE, 48)
        .fixed(Material.PORKCHOP, 64)
        .fixed(Material.BEEF, 64)
        .fixed(Material.CHICKEN, 64)
        .fixed(Material.RABBIT, 64)
        .fixed(Material.MUTTON, 64)
        .fixed(Material.COD, 64)
        .fixed(Material.SALMON, 64)
        .fixed(Material.TROPICAL_FISH, 64)
        .fixed(Material.PUFFERFISH, 64)
        .fixed(Material.CARROT, 64)
        .fixed(Material.BEETROOT, 64)
        .fixed(Material.POTATO, 64)
        .fixed(Material.POISONOUS_POTATO, 64)
        .fixed(Material.STRING, 12)
        .fixed(Material.ROTTEN_FLESH, 32)
        .fixed(Material.SLIME_BALL, 32)
        .fixed(Material.EGG, 32)
        .fixed(Material.TURTLE_SCUTE, 96)
        .fixed(Material.TURTLE_EGG, 192)
        .fixed(Tag.SNAPS_GOAT_HORN, 192)
        .fixed(Material.FEATHER, 48)
        .fixed(Material.RABBIT_HIDE, 16)
        .fixed(Material.RABBIT_FOOT, 128)
        .fixed(Material.SPIDER_EYE, 128)
        .fixed(Material.PHANTOM_MEMBRANE, 192)
        .fixed(Material.GUNPOWDER, 192)
        .fixed(Material.SKELETON_SKULL, 256)
        .fixed(Material.ZOMBIE_HEAD, 256)
        .fixed(Material.CREEPER_HEAD, 256)
        .fixed(Material.PIGLIN_HEAD, 256)
        .fixed(Material.PIGLIN_BANNER_PATTERN, 512)
        .fixed(Material.FLOW_BANNER_PATTERN, 20_480)
        .fixed(Material.GUSTER_BANNER_PATTERN, 12_224)
        .fixed(Material.ENDER_PEARL, 1_024)
        .fixed(Material.NAUTILUS_SHELL, 1_024)
        .fixed(Material.BLAZE_ROD, 1_536)
        .fixed(Material.BREEZE_ROD, 2_304)
        .fixed(Material.SHULKER_SHELL, 2_048)
        .fixed(Material.SNIFFER_EGG, 2_048)
        .fixed(Material.GHAST_TEAR, 4_096)
        .fixed(Material.TRIDENT, 16_398)
        .fixed(Material.HEART_OF_THE_SEA, 32_768)
        .fixed(Material.HEAVY_CORE, 40_960)
        .fixed(Material.DRAGON_EGG, 262_144)
        .fixed(Material.SADDLE, 192)
        .fixed(Material.ECHO_SHARD, 192)
        .fixed(Material.NAME_TAG, 192)

        .fixed(Material.MUSIC_DISC_13, 2_048)
        .fixed(Material.MUSIC_DISC_CAT, 2_048)
        .fixed(Material.MUSIC_DISC_BLOCKS, 2_048)
        .fixed(Material.MUSIC_DISC_CHIRP, 2_048)
        .fixed(Material.MUSIC_DISC_FAR, 2_048)
        .fixed(Material.MUSIC_DISC_MALL, 2_048)
        .fixed(Material.MUSIC_DISC_MELLOHI, 2_048)
        .fixed(Material.MUSIC_DISC_STAL, 2_048)
        .fixed(Material.MUSIC_DISC_STRAD, 2_048)
        .fixed(Material.MUSIC_DISC_WARD, 2_048)
        .fixed(Material.MUSIC_DISC_11, 2_048)
        .fixed(Material.MUSIC_DISC_WAIT, 2_048)

        .fixed(Material.DISC_FRAGMENT_5, 192)
        .fixed(Material.MUSIC_DISC_CREATOR, 40_960)
        .fixed(Material.MUSIC_DISC_CREATOR_MUSIC_BOX, 8_192)
        .fixed(Material.MUSIC_DISC_OTHERSIDE, 6_144)
        .fixed(Material.MUSIC_DISC_PIGSTEP, 8_192)
        .fixed(Material.MUSIC_DISC_PRECIPICE, 12_224)
        .fixed(Material.MUSIC_DISC_RELIC, 10_176)
        .fixed(Material.FLINT, 4)
        .fixed(Material.COAL, 128)
        .fixed(Material.QUARTZ, 256)
        .fixed(Material.PRISMARINE_SHARD, 256)
        .fixed(Material.PRISMARINE_CRYSTALS, 512)
        .fixed(Material.INK_SAC, 16)
        .fixed(Material.COCOA_BEANS, 64)
        .fixed(Material.LAPIS_LAZULI, 864)
        .fixed(Material.EMERALD, 16_384)
        .fixed(Material.NETHER_STAR, 139_264)
        .fixed(Material.CLAY_BALL, 16)

        .fixed(Material.ANGLER_POTTERY_SHERD, 216)
        .fixed(Material.ARCHER_POTTERY_SHERD, 216)
        .fixed(Material.ARMS_UP_POTTERY_SHERD, 216)
        .fixed(Material.BLADE_POTTERY_SHERD, 216)
        .fixed(Material.BREWER_POTTERY_SHERD, 216)
        .fixed(Material.BURN_POTTERY_SHERD, 216)
        .fixed(Material.DANGER_POTTERY_SHERD, 216)
        .fixed(Material.EXPLORER_POTTERY_SHERD, 216)
        .fixed(Material.FRIEND_POTTERY_SHERD, 216)
        .fixed(Material.GUSTER_POTTERY_SHERD, 216)
        .fixed(Material.HEART_POTTERY_SHERD, 216)
        .fixed(Material.HEARTBREAK_POTTERY_SHERD, 216)
        .fixed(Material.HOWL_POTTERY_SHERD, 216)
        .fixed(Material.MINER_POTTERY_SHERD, 216)
        .fixed(Material.MOURNER_POTTERY_SHERD, 216)
        .fixed(Material.PLENTY_POTTERY_SHERD, 216)
        .fixed(Material.PRIZE_POTTERY_SHERD, 216)
        .fixed(Material.SHEAF_POTTERY_SHERD, 216)
        .fixed(Material.SHELTER_POTTERY_SHERD, 216)
        .fixed(Material.SKULL_POTTERY_SHERD, 216)
        .fixed(Material.SNORT_POTTERY_SHERD, 216)

        .fixed(Material.BONE, 144)
        .fixed(Material.SNOWBALL, 1)
        .fixed(Material.FILLED_MAP, 1_472)
        .fixed(Tag.LOGS, 32)
        .fixed(Tag.PLANKS, 8)
        .fixed(Tag.SAPLINGS, 32)
        .fixed(Material.STICK, 4)
        .fixed(Tag.LEAVES, 1)
        .fixed(Material.MANGROVE_ROOTS, 4)
        .fixed(Tag.WOOL, 48)
        .fixed(Material.ARMADILLO_SCUTE, 48)
        .fixed(Material.NETHERITE_SCRAP, 12_288)
        .fixed(Material.DIAMOND, 8_192)
        .fixed(Material.REDSTONE, 64)
        .fixed(Material.GLOWSTONE, 384)
        .fixed(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 7_497)
        .fixed(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 23_017)
        .fixed(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 57_345)
        .fixed(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 53_898)
        .fixed(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 51_917)
        .fixed(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 42_641)
        .fixed(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, 39_465)
        .fixed(Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, 22_116)
        .fixed(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 19_677)
        .fixed(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 18_588)
        .fixed(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 12_271)
        .fixed(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 10_310)
        .fixed(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176)
        .fixed(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176)
        .fixed(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176)
        .fixed(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176)
        .fixed(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 7_533)
        .fixed(Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, 30_528)
        .fixed(Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, 10_176)
        ;
    }

    public void buildHardcodedConversion(WorldTransmutationsBuilder worldTransmutationsBuilder) {
        this
            // FROM PROJECTE: https://github.com/sinkillerj/ProjectE/blob/15d4ce65bd06eb4222709b984255fbf5080e78bc/src/datagen/java/moze_intel/projecte/common/PECustomConversionProvider.java
            .conversion(Material.GOLD_INGOT)
                .addIngredient(Material.IRON_INGOT, 8)
                .end()
            .conversion(Material.COPPER_INGOT)
                .addIngredient(Material.IRON_INGOT)
                .end()

            .conversion(Material.GRASS_BLOCK)
                .addIngredient(Material.DIRT, 2)
                .end()
            .conversion(Material.PODZOL)
                .addIngredient(Material.DIRT, 2)
                .end()
            .conversion(Material.ROOTED_DIRT)
                .addIngredient(Material.DIRT)
                .addIngredient(Material.HANGING_ROOTS)
                .end()
            .conversion(Material.MYCELIUM)
                .addIngredient(Material.DIRT, 2)
                .end()
            .conversion(Material.MUD)
                .addIngredient(Material.DIRT)
                .addIngredient(Material.POTION)
                .addIngredient(Material.GLASS_BOTTLE, -1)
                .end()
            .conversion(Material.CRIMSON_NYLIUM)
                .addIngredient(Material.NETHERRACK, 2)
                .end()
            .conversion(Material.WARPED_NYLIUM)
                .addIngredient(Material.NETHERRACK, 2)
                .end()
            .conversion(Material.IRON_HORSE_ARMOR)
                .addIngredient(Material.IRON_INGOT, 8)
                .end()
            .conversion(Material.GOLDEN_HORSE_ARMOR)
                .addIngredient(Material.GOLD_INGOT, 8)
                .end()
            .conversion(Material.DIAMOND_HORSE_ARMOR)
                .addIngredient(Material.DIAMOND, 8)
                .end()
            .conversion(Material.CARVED_PUMPKIN)
                .addIngredient(Material.PUMPKIN)
                .end()
            .conversion(Material.TORCHFLOWER_SEEDS)
                .addIngredient(Material.TORCHFLOWER)
                .end()
            .conversion(Material.PITCHER_POD)
                .addIngredient(Material.PITCHER_PLANT)
                .end()
            .conversion(Material.WRITTEN_BOOK)
                .addIngredient(Material.WRITABLE_BOOK)
                .end()
            .conversion(Material.ENCHANTED_BOOK)
                .addIngredient(Material.BOOK)
                .end()
            .conversion(Material.ENCHANTED_GOLDEN_APPLE)
                .addIngredient(Material.APPLE)
                .addIngredient(Material.GOLD_BLOCK, 8)
                .end()
            .conversion(Material.STRIPPED_BAMBOO_BLOCK)
                .addIngredient(Material.BAMBOO_BLOCK)
                .end()
            .conversion(Material.GLOBE_BANNER_PATTERN)
                .addIngredient(Material.EMERALD, 3)
                .end()
            .conversion(Material.SKULL_BANNER_PATTERN, 3)
                .addIngredient(Material.PAPER, 3)
                .addIngredient(Material.NETHER_STAR)
                .addIngredient(Material.SOUL_SAND, -4)
                .end()
            .conversion(Material.FIREWORK_STAR)
                .addIngredient(Material.GUNPOWDER)
                .end()
            .conversion(Material.FIREWORK_ROCKET)
                .addIngredient(Material.PAPER)
                .addIngredient(Material.GUNPOWDER)
                .end()
            .conversion(Material.SUSPICIOUS_GRAVEL)
                .addIngredient(Material.GRAVEL)
                .end()
            .conversion(Material.SUSPICIOUS_SAND)
                .addIngredient(Material.SAND)
                .end()
            .conversion(Material.WET_SPONGE)
                .addIngredient(Material.SPONGE)
                .end()
            .conversion(Material.BELL)
                .addIngredient(Material.GOLD_INGOT, 7)
                .end()
            .conversion(Material.HONEYCOMB, 3)
                .addIngredient(Material.HONEY_BOTTLE)
                .end()
            .conversion(Material.POWDER_SNOW_BUCKET)
                .addIngredient(Material.BUCKET)
                .addIngredient(Material.SNOW_BLOCK, 4)
                .end()
            .conversion(Material.GLOW_INK_SAC)
                .addIngredient(Material.INK_SAC)
                .addIngredient(Material.GLOWSTONE_DUST)
                .end()
            .conversion(Material.HANGING_ROOTS)
                .addIngredient(Material.STICK)
                .end()
            .conversion(Material.SUSPICIOUS_STEW)
                .addIngredient(Material.BOWL)
                .addIngredient(Material.BROWN_MUSHROOM)
                .addIngredient(Material.RED_MUSHROOM)
                .addIngredient(Material.WITHER_ROSE)
                .end()
            .conversion(Material.PUFFERFISH_BUCKET)
                .addIngredient(Material.WATER_BUCKET)
                .addIngredient(Material.PUFFERFISH)
                .end()
            .conversion(Material.SALMON_BUCKET)
                .addIngredient(Material.WATER_BUCKET)
                .addIngredient(Material.SALMON)
                .end()
            .conversion(Material.COD_BUCKET)
                .addIngredient(Material.WATER_BUCKET)
                .addIngredient(Material.COD)
                .end()
            .conversion(Material.TROPICAL_FISH_BUCKET)
                .addIngredient(Material.WATER_BUCKET)
                .addIngredient(Material.TROPICAL_FISH)
                .end()
            .conversion(Material.DRAGON_BREATH)
                .addIngredient(Material.GLASS_BOTTLE)
                .addValue(1)
                .end()
            .conversion(Material.SOUL_SOIL)
                .addIngredient(Material.SOUL_SAND)
                .end()
            .conversion(Material.WARPED_WART_BLOCK)
                .addIngredient(Material.NETHER_WART_BLOCK)
                .end()
            .conversion(Material.SHROOMLIGHT)
                .addIngredient(Material.GLOWSTONE_DUST)
                .addIngredient(Material.RED_MUSHROOM)
                .end()
            .conversion(Material.OCHRE_FROGLIGHT, 3)
                .addIngredient(Material.MAGMA_CREAM)
                .end()
            .conversion(Material.PEARLESCENT_FROGLIGHT, 3)
                .addIngredient(Material.MAGMA_CREAM)
                .end()
            .conversion(Material.VERDANT_FROGLIGHT, 3)
                .addIngredient(Material.MAGMA_CREAM)
                .end()
            .conversion(Material.SCULK)
                .addIngredient(Material.SCULK_VEIN, 4)
                .end()
            .conversion(Material.SCULK_SENSOR)
                .addIngredient(Material.COMPARATOR, 4)
                .addIngredient(Material.REPEATER, 4)
                .addIngredient(Material.SCULK, 1)
                .end()
            .conversion(Material.SCULK_SHRIEKER)
                .addIngredient(Material.SCULK_CATALYST, 4)
                .end()
            .conversion(Material.DIRT_PATH)
                .addIngredient(Material.GRASS_BLOCK)
                .end()
            .conversion(Material.DIRT_PATH)
                .addIngredient(Material.DIRT)
                .end()
            .conversion(Material.DIRT_PATH)
                .addIngredient(Material.PODZOL)
                .end()
            .conversion(Material.DIRT_PATH)
                .addIngredient(Material.COARSE_DIRT)
                .end()
            .conversion(Material.DIRT_PATH)
                .addIngredient(Material.MYCELIUM)
                .end()
            .conversion(Material.DIRT_PATH)
                .addIngredient(Material.ROOTED_DIRT)
                .end()
            .conversion(Material.CHAINMAIL_HELMET)
                .addIngredient(Material.IRON_HELMET)
                .end()
            .conversion(Material.CHAINMAIL_CHESTPLATE)
                .addIngredient(Material.IRON_CHESTPLATE)
                .end()
            .conversion(Material.CHAINMAIL_LEGGINGS)
                .addIngredient(Material.IRON_LEGGINGS)
                .end()
            .conversion(Material.CHAINMAIL_BOOTS)
                .addIngredient(Material.IRON_BOOTS)
                .end()
            .conversion(Material.WHITE_CONCRETE)
                .addIngredient(Material.WHITE_CONCRETE_POWDER)
                .end()
            .conversion(Material.ORANGE_CONCRETE)
                .addIngredient(Material.ORANGE_CONCRETE_POWDER)
                .end()
            .conversion(Material.MAGENTA_CONCRETE)
                .addIngredient(Material.MAGENTA_CONCRETE_POWDER)
                .end()
            .conversion(Material.LIGHT_BLUE_CONCRETE)
                .addIngredient(Material.LIGHT_BLUE_CONCRETE_POWDER)
                .end()
            .conversion(Material.YELLOW_CONCRETE)
                .addIngredient(Material.YELLOW_CONCRETE_POWDER)
                .end()
            .conversion(Material.LIME_CONCRETE)
                .addIngredient(Material.LIME_CONCRETE_POWDER)
                .end()
            .conversion(Material.PINK_CONCRETE)
                .addIngredient(Material.PINK_CONCRETE_POWDER)
                .end()
            .conversion(Material.GRAY_CONCRETE)
                .addIngredient(Material.GRAY_CONCRETE_POWDER)
                .end()
            .conversion(Material.LIGHT_GRAY_CONCRETE)
                .addIngredient(Material.LIGHT_GRAY_CONCRETE_POWDER)
                .end()
            .conversion(Material.CYAN_CONCRETE)
                .addIngredient(Material.CYAN_CONCRETE_POWDER)
                .end()
            .conversion(Material.PURPLE_CONCRETE)
                .addIngredient(Material.PURPLE_CONCRETE_POWDER)
                .end()
            .conversion(Material.BLUE_CONCRETE)
                .addIngredient(Material.BLUE_CONCRETE_POWDER)
                .end()
            .conversion(Material.BROWN_CONCRETE)
                .addIngredient(Material.BROWN_CONCRETE_POWDER)
                .end()
            .conversion(Material.GREEN_CONCRETE)
                .addIngredient(Material.GREEN_CONCRETE_POWDER)
                .end()
            .conversion(Material.RED_CONCRETE)
                .addIngredient(Material.RED_CONCRETE_POWDER)
                .end()
            .conversion(Material.BLACK_CONCRETE)
                .addIngredient(Material.BLACK_CONCRETE_POWDER)
                .end()
            .conversion(Material.CHIPPED_ANVIL, 25)
                .addIngredient(Material.ANVIL, 16)
                .end()
            .conversion(Material.DAMAGED_ANVIL, 25)
                .addIngredient(Material.ANVIL, 7)
                .end()
            .conversion(Material.WATER_BUCKET)
                .addIngredient(Material.BUCKET)
                .end()
            .conversion(Material.LAVA_BUCKET)
                .addIngredient(Material.BUCKET)
                .addIngredient(Material.OBSIDIAN)
                .end()
            .conversion(Material.MILK_BUCKET)
                .addIngredient(Material.BUCKET)
                .addValue(16)
                .end()

            .conversion(Material.GLOWSTONE_DUST, 4)
                .addIngredient(Material.GLOWSTONE)
                .end()
        ;

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
}
