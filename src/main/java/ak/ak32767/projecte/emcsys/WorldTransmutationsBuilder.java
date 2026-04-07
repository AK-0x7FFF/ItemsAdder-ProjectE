package ak.ak32767.projecte.emcsys;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class WorldTransmutationsBuilder {
    private final ArrayList<WorldTransmutationNode> registered;

    public WorldTransmutationsBuilder() {
        this.registered = new ArrayList<>();
    }

    public void build() {
        this
        .register(Material.STONE, Material.COBBLESTONE, Material.GRASS_BLOCK)
        .register(Material.COBBLESTONE, Material.STONE, Material.GRASS_BLOCK)
        .register(Material.GRASS_BLOCK, Material.SAND, Material.COBBLESTONE)
        .register(Material.SAND, Material.GRASS_BLOCK, Material.COBBLESTONE)
        .register(Material.DIRT, Material.SAND, Material.COBBLESTONE)
        .registerChain(Material.GRAVEL, Material.SANDSTONE)
        .registerChain(Material.FROSTED_ICE, Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE)
        .registerChain(Material.OBSIDIAN, Material.LAVA)
        .registerChain(Material.MELON, Material.PUMPKIN)
        .register(Material.GRANITE, Material.DIORITE, Material.ANDESITE)
        .register(Material.DIORITE, Material.ANDESITE, Material.GRANITE)
        .register(Material.ANDESITE, Material.GRANITE, Material.DIORITE)

        .registerChain(Material.SOUL_SAND, Material.SOUL_SOIL)
        .registerChain(Material.NETHERRACK, Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM)
        .registerChain(Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK)
        .registerChain(Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS)
        .registerChain(Material.CRIMSON_ROOTS, Material.WARPED_ROOTS)

        .registerChain(Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG)
        .registerChain(Material.STRIPPED_OAK_LOG, Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG)
        .registerChain(Material.OAK_WOOD, Material.BIRCH_WOOD, Material.SPRUCE_WOOD, Material.JUNGLE_WOOD, Material.ACACIA_WOOD, Material.DARK_OAK_WOOD, Material.MANGROVE_WOOD, Material.CHERRY_WOOD)
        .registerChain(Material.STRIPPED_OAK_WOOD, Material.STRIPPED_BIRCH_WOOD, Material.STRIPPED_SPRUCE_WOOD, Material.STRIPPED_JUNGLE_WOOD, Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_DARK_OAK_WOOD, Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_CHERRY_WOOD)
        .registerChain(Material.OAK_LEAVES, Material.BIRCH_LEAVES, Material.SPRUCE_LEAVES, Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES)
        .registerChain(Material.OAK_SAPLING, Material.BIRCH_SAPLING, Material.SPRUCE_SAPLING, Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING, Material.MANGROVE_PROPAGULE, Material.CHERRY_SAPLING)
        .registerChain(Material.OAK_PLANKS, Material.BIRCH_PLANKS, Material.SPRUCE_PLANKS, Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS, Material.MANGROVE_PLANKS, Material.CHERRY_PLANKS, Material.BAMBOO_PLANKS)
        .registerChain(Material.OAK_SLAB, Material.BIRCH_SLAB, Material.SPRUCE_SLAB, Material.JUNGLE_SLAB, Material.ACACIA_SLAB, Material.DARK_OAK_SLAB, Material.MANGROVE_SLAB, Material.CHERRY_SLAB, Material.BAMBOO_SLAB)
        .registerChain(Material.OAK_STAIRS, Material.BIRCH_STAIRS, Material.SPRUCE_STAIRS, Material.JUNGLE_STAIRS, Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS, Material.MANGROVE_STAIRS, Material.CHERRY_STAIRS, Material.BAMBOO_STAIRS)
        .registerChain(Material.OAK_FENCE, Material.BIRCH_FENCE, Material.SPRUCE_FENCE, Material.JUNGLE_FENCE, Material.ACACIA_FENCE, Material.DARK_OAK_FENCE, Material.MANGROVE_FENCE, Material.CHERRY_FENCE, Material.BAMBOO_FENCE)
        .registerChain(Material.OAK_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE, Material.MANGROVE_PRESSURE_PLATE, Material.CHERRY_PRESSURE_PLATE, Material.BAMBOO_PRESSURE_PLATE)
        .registerChain(Material.OAK_BUTTON, Material.BIRCH_BUTTON, Material.SPRUCE_BUTTON, Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON,  Material.MANGROVE_BUTTON, Material.CHERRY_BUTTON, Material.BAMBOO_BUTTON)
        .registerChain(Material.OAK_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.JUNGLE_TRAPDOOR, Material.ACACIA_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.MANGROVE_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.BAMBOO_TRAPDOOR)
        .registerChain(Material.OAK_DOOR, Material.BIRCH_DOOR, Material.SPRUCE_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.MANGROVE_DOOR, Material.CHERRY_DOOR, Material.BAMBOO_DOOR)
        .registerChain(Material.OAK_SIGN, Material.BIRCH_SIGN, Material.SPRUCE_SIGN, Material.JUNGLE_SIGN, Material.ACACIA_SIGN, Material.DARK_OAK_SIGN, Material.MANGROVE_SIGN, Material.CHERRY_SIGN, Material.BAMBOO_SIGN)
        .registerChain(Material.OAK_WALL_SIGN, Material.BIRCH_WALL_SIGN, Material.SPRUCE_WALL_SIGN, Material.JUNGLE_WALL_SIGN, Material.ACACIA_WALL_SIGN, Material.DARK_OAK_WALL_SIGN, Material.MANGROVE_WALL_SIGN, Material.CHERRY_WALL_SIGN, Material.BAMBOO_WALL_SIGN)
        .registerChain(Material.OAK_HANGING_SIGN, Material.BIRCH_HANGING_SIGN, Material.SPRUCE_HANGING_SIGN, Material.JUNGLE_HANGING_SIGN, Material.ACACIA_HANGING_SIGN, Material.DARK_OAK_HANGING_SIGN, Material.MANGROVE_HANGING_SIGN, Material.CHERRY_HANGING_SIGN, Material.BAMBOO_HANGING_SIGN)
        .registerChain(Material.OAK_WALL_HANGING_SIGN, Material.BIRCH_WALL_HANGING_SIGN, Material.SPRUCE_WALL_HANGING_SIGN, Material.JUNGLE_WALL_HANGING_SIGN, Material.ACACIA_WALL_HANGING_SIGN, Material.DARK_OAK_WALL_HANGING_SIGN, Material.MANGROVE_WALL_HANGING_SIGN, Material.CHERRY_WALL_HANGING_SIGN, Material.BAMBOO_WALL_HANGING_SIGN)

        .registerChain(Material.CRIMSON_STEM, Material.WARPED_STEM)
        .registerChain(Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM)
        .registerChain(Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE)
        .registerChain(Material.STRIPPED_CRIMSON_HYPHAE, Material.STRIPPED_WARPED_HYPHAE)
        .registerChain(Material.CRIMSON_PLANKS, Material.WARPED_PLANKS)
        .registerChain(Material.CRIMSON_SLAB, Material.WARPED_SLAB)
        .registerChain(Material.CRIMSON_STAIRS, Material.WARPED_STAIRS)
        .registerChain(Material.CRIMSON_FENCE, Material.WARPED_FENCE)
        .registerChain(Material.CRIMSON_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE)
        .registerChain(Material.CRIMSON_BUTTON, Material.WARPED_BUTTON)
        .registerChain(Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR)
        .registerChain(Material.CRIMSON_DOOR, Material.WARPED_DOOR)
        .registerChain(Material.CRIMSON_SIGN, Material.WARPED_SIGN)
        .registerChain(Material.CRIMSON_WALL_SIGN, Material.WARPED_WALL_SIGN)
        .registerChain(Material.CRIMSON_HANGING_SIGN, Material.WARPED_HANGING_SIGN)
        .registerChain(Material.CRIMSON_WALL_HANGING_SIGN, Material.WARPED_WALL_HANGING_SIGN)

        .registerChain(Material.WHITE_CONCRETE, Material.ORANGE_CONCRETE, Material.MAGENTA_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE, Material.PINK_CONCRETE, Material.GRAY_CONCRETE, Material.LIGHT_GRAY_CONCRETE, Material.CYAN_CONCRETE, Material.PURPLE_CONCRETE, Material.BLUE_CONCRETE, Material.BROWN_CONCRETE, Material.GREEN_CONCRETE, Material.RED_CONCRETE, Material.BLACK_CONCRETE)
        .registerChain(Material.WHITE_CONCRETE_POWDER, Material.ORANGE_CONCRETE_POWDER, Material.MAGENTA_CONCRETE_POWDER, Material.LIGHT_BLUE_CONCRETE_POWDER, Material.YELLOW_CONCRETE_POWDER, Material.LIME_CONCRETE_POWDER, Material.PINK_CONCRETE_POWDER, Material.GRAY_CONCRETE_POWDER, Material.LIGHT_GRAY_CONCRETE_POWDER, Material.CYAN_CONCRETE_POWDER, Material.PURPLE_CONCRETE_POWDER, Material.BLUE_CONCRETE_POWDER, Material.BROWN_CONCRETE_POWDER, Material.GREEN_CONCRETE_POWDER, Material.RED_CONCRETE_POWDER, Material.BLACK_CONCRETE_POWDER)
        .registerChain(Material.WHITE_CARPET, Material.ORANGE_CARPET, Material.MAGENTA_CARPET, Material.LIGHT_BLUE_CARPET, Material.YELLOW_CARPET, Material.LIME_CARPET, Material.PINK_CARPET, Material.GRAY_CARPET, Material.LIGHT_GRAY_CARPET, Material.CYAN_CARPET, Material.PURPLE_CARPET, Material.BLUE_CARPET, Material.BROWN_CARPET, Material.GREEN_CARPET, Material.RED_CARPET, Material.BLACK_CARPET)
        .registerChain(Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL)
        .registerChain(Material.WHITE_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.MAGENTA_TERRACOTTA, Material.LIGHT_BLUE_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.LIME_TERRACOTTA, Material.PINK_TERRACOTTA, Material.GRAY_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA, Material.CYAN_TERRACOTTA, Material.PURPLE_TERRACOTTA, Material.BLUE_TERRACOTTA, Material.BROWN_TERRACOTTA, Material.GREEN_TERRACOTTA, Material.RED_TERRACOTTA, Material.BLACK_TERRACOTTA)
        .registerChain(Material.WHITE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.PINK_STAINED_GLASS, Material.GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS, Material.CYAN_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.BROWN_STAINED_GLASS, Material.GREEN_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.BLACK_STAINED_GLASS)
        .registerChain(Material.WHITE_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.PINK_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.LIGHT_GRAY_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.BROWN_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE)
        ;


        // ojang搞得什麼銅玩意
        final String WAXED = "WAXED_";
        final String[] COPPER_BLOCKS = {
                "COPPER_BLOCK", "COPPER_BULB", "COPPER_DOOR", "COPPER_GRATE", "COPPER_TRAPDOOR",
                "CUT_COPPER", "CUT_COPPER_SLAB", "CUT_COPPER_STAIRS", "CHISELED_COPPER"
        };
        final String[] STAGE = {"", "EXPOSED_", "WEATHERED_", "OXIDIZED_"};

        for (String material_name : COPPER_BLOCKS) {
            Material[] materials = Arrays.stream(STAGE)
                    .map(stage -> Material.getMaterial(stage + material_name))
                    .filter(Objects::nonNull)
                    .toArray(Material[]::new);
            registerChain(materials);

            Material[] waxed_materials = Arrays.stream(materials)
                    .map(material -> Material.getMaterial(WAXED + material.name()))
                    .filter(Objects::nonNull)
                    .toArray(Material[]::new);
            registerChain(waxed_materials);
        }

    }

    public WorldTransmutationsBuilder register(Material origin, Material result) {
        WorldTransmutationNode node = new WorldTransmutationNode(origin, result, result);
        this.registered.add(node);
        return this;
    }

    public WorldTransmutationsBuilder register(Material origin, Material resultForward, Material resultBackward) {
        WorldTransmutationNode node = new WorldTransmutationNode(origin, resultForward, resultBackward);
        this.registered.add(node);
        return this;
    }

    public WorldTransmutationsBuilder registerChain(Material... materials) {
        for (int i = 0; i < materials.length; ++i) {
            Material orijin   = materials[i];
            Material forward  = materials[i == materials.length - 1 ? 0 : i + 1];
            Material backward = materials[i == 0 ? materials.length - 1 : i - 1];

            WorldTransmutationNode node = new WorldTransmutationNode(orijin, forward, backward);
            this.registered.add(node);
        }

        return this;
    }

    public ArrayList<WorldTransmutationNode> getRegistered() {
        return this.registered;
    }

    public class WorldTransmutationNode {
        private final Material origin;
        private final Material resultForward;
        private final Material resultBackward;

        public WorldTransmutationNode(Material origin, Material resultForward, Material resultBackward) {
            this.origin = origin;
            this.resultForward = resultForward;
            this.resultBackward = resultBackward;
        }

        public Material getOrigin() {
            return origin;
        }

        public Material getResultForward() {
            return resultForward;
        }

        public Material getResultBackward() {
            return resultBackward;
        }
    }
}
