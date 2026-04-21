package ak.ak32767.projecte.emcsys;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.ProjectEException;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.utils.YAMLLoader;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class WorldTransmutationsBuilder {
    public record WorldTransmutationNode(
        ItemWrapper.TransmutableItem origin, ItemWrapper.TransmutableItem resultForward, ItemWrapper.TransmutableItem resultBackward
    ) {}

    private final ProjectE plugin;
    private final Set<WorldTransmutationNode> conversions;

    public WorldTransmutationsBuilder(ProjectE plugin) {
        this.plugin = plugin;
        this.conversions = new ObjectLinkedOpenHashSet<>();
    }

    public boolean build() {
        try {
            FileConfiguration config = YAMLLoader.loadResource(this.plugin, "data/transmutation_data.yml");
            buildByYAML(config.getMapList("world"));
            buildHardcodeCopper();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void buildByYAML(List<Map<?, ?>> transList) throws ProjectEException.YAMLKeyOrValueErrorException {
        for (Map<?, ?> entry : transList) {
            String type = (String) entry.get("type");
            if (type == null) throw new ProjectEException.YAMLKeyOrValueErrorException("type is null");

            if (type.equals("REGULAR")) {
                var origin = YAMLLoader.ItemYAMLWrapper.of((Map<?, ?>) entry.get("origin"));
                var resultForward = YAMLLoader.ItemYAMLWrapper.of((Map<?, ?>) entry.get("result_forward"));

                Map<?, ?> resultBackwardEntry = (Map<?, ?>) entry.get("result_backward");
                if (resultBackwardEntry == null) {
                    this.register(origin.getFirst(), resultForward.getFirst());
                    continue;
                }

                var resultBackward = YAMLLoader.ItemYAMLWrapper.of(resultBackwardEntry);
                this.register(origin.getFirst(), resultForward.getFirst(), resultBackward.getFirst());

            } else if (type.equals("CHAIN")) {
                List<Map<?, ?>> items = (List<Map<?, ?>>) entry.get("items");

                List<ItemWrapper.TransmutableItem> itemList = items.stream()
                    .map(item -> {
                        try { return YAMLLoader.ItemYAMLWrapper.of(item).getFirst(); }
                        catch (ProjectEException.YAMLKeyOrValueErrorException e) { return null; }
                    })
                    .toList();

                if (itemList.contains(null))
                    throw new ProjectEException.YAMLKeyOrValueErrorException("Item list has null");

                this.registerChain(itemList.toArray(ItemWrapper.TransmutableItem[]::new));
            }
        }
    }

    private void buildHardcodeCopper() {
        final String WAXED = "WAXED_";
        final String[] COPPER_BLOCKS = {
                "COPPER_BLOCK", "COPPER_BULB", "COPPER_DOOR", "COPPER_GRATE", "COPPER_TRAPDOOR",
                "CUT_COPPER", "CUT_COPPER_SLAB", "CUT_COPPER_STAIRS", "CHISELED_COPPER"
        };
        final String[] STAGE = {"", "EXPOSED_", "WEATHERED_", "OXIDIZED_"};

        for (String material_name : COPPER_BLOCKS) {
            ItemWrapper.MaterialItem[] materials = Arrays.stream(STAGE)
                    .map(stage -> Material.getMaterial(stage + material_name))
                    .filter(Objects::nonNull)
                    .map(ItemWrapper.MaterialItem::new)
                    .toArray(ItemWrapper.MaterialItem[]::new);
            registerChain(materials);

            ItemWrapper.MaterialItem[] waxed_materials = Arrays.stream(materials)
                    .map(material -> Material.getMaterial(WAXED + material.material().name()))
                    .filter(Objects::nonNull)
                    .map(ItemWrapper.MaterialItem::new)
                    .toArray(ItemWrapper.MaterialItem[]::new);
            registerChain(waxed_materials);
        }
    }


    public WorldTransmutationsBuilder register(ItemWrapper.TransmutableItem origin, ItemWrapper.TransmutableItem result) {
        WorldTransmutationNode node = new WorldTransmutationNode(origin, result, result);
        this.conversions.add(node);
        return this;
    }

    public WorldTransmutationsBuilder register(ItemWrapper.TransmutableItem origin, ItemWrapper.TransmutableItem resultForward, ItemWrapper.TransmutableItem resultBackward) {
        WorldTransmutationNode node = new WorldTransmutationNode(origin, resultForward, resultBackward);
        this.conversions.add(node);
        return this;
    }

    public WorldTransmutationsBuilder registerChain(ItemWrapper.TransmutableItem... items) {
        for (int i = 0; i < items.length; ++i) {
            ItemWrapper.TransmutableItem orijin   = items[i];
            ItemWrapper.TransmutableItem forward  = items[i == items.length - 1 ? 0 : i + 1];
            ItemWrapper.TransmutableItem backward = items[i == 0 ? items.length - 1 : i - 1];

            WorldTransmutationNode node = new WorldTransmutationNode(orijin, forward, backward);
            this.conversions.add(node);
        }

        return this;
    }

    public Set<WorldTransmutationNode> getConversions() {
        return this.conversions;
    }
}
