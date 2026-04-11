package ak.ak32767.projecte.emcsys;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.utils.YAMLLoader;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.*;

public class WorldTransmutationsBuilder {
    public record WorldTransmutationNode(ItemWrapper.TransmutableItem origin, ItemWrapper.TransmutableItem resultForward, ItemWrapper.TransmutableItem resultBackward) {}
    private final ArrayList<WorldTransmutationNode> registered;

    private final ProjectE plugin;
    public WorldTransmutationsBuilder(ProjectE plugin) {
        this.plugin = plugin;
        this.registered = new ArrayList<>();
    }

    public void buildx() throws FileNotFoundException {
        FileConfiguration config = YAMLLoader.load(this.plugin, "data/transmutation_data.yml");
        if (config == null)
            return;

        @NotNull List<Map<?, ?>> transMap = config.getMapList("world");
        buildByYAML(transMap);
        buildHardcodeCopper();
    }


    public void buildByYAML(List<Map<?, ?>> transList) {
        for (Map<?, ?> entry : transList) {
            String type = (String) entry.get("type");
            if (type == null) throw new IllegalArgumentException("type is null");

            if (type.equals("REGULAR")) {
                List<ItemWrapper.TransmutableItem> origin = YAMLLoader.ItemYAMLWrapper.of((Map<?, ?>) entry.get("origin"));
                if (origin.isEmpty()) throw new IllegalArgumentException("origin is null");

                List<ItemWrapper.TransmutableItem> resultForward = YAMLLoader.ItemYAMLWrapper.of((Map<?, ?>) entry.get("result_forward"));
                if (resultForward.isEmpty()) throw new IllegalArgumentException("result_forward is null");

                Map<?, ?> resultBackwardEntry = (Map<?, ?>) entry.get("result_backward");
                if (resultBackwardEntry == null) {
                    this.register(origin.getFirst(), resultForward.getFirst());
                    continue;
                }

                List<ItemWrapper.TransmutableItem> resultBackward = YAMLLoader.ItemYAMLWrapper.of(resultBackwardEntry);
                if (resultBackward.isEmpty()) throw new IllegalArgumentException("result_backward is null");
                this.register(origin.getFirst(), resultForward.getFirst(), resultBackward.getFirst());

            } else if (type.equals("CHAIN")) {
                List<Map<?, ?>> items = (List<Map<?, ?>>) entry.get("items");
                if (items == null || items.isEmpty()) throw new IllegalArgumentException("items is null");

                List<ItemWrapper.TransmutableItem> itemList = items.stream()
                    .map(item -> YAMLLoader.ItemYAMLWrapper.of(item).getFirst())
                    .toList();

                if (itemList.contains(null))
                    throw new IllegalArgumentException("items has null");

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
        this.registered.add(node);
        return this;
    }

    public WorldTransmutationsBuilder register(ItemWrapper.TransmutableItem origin, ItemWrapper.TransmutableItem resultForward, ItemWrapper.TransmutableItem resultBackward) {
        WorldTransmutationNode node = new WorldTransmutationNode(origin, resultForward, resultBackward);
        this.registered.add(node);
        return this;
    }

    public WorldTransmutationsBuilder registerChain(ItemWrapper.TransmutableItem... items) {
        for (int i = 0; i < items.length; ++i) {
            ItemWrapper.TransmutableItem orijin   = items[i];
            ItemWrapper.TransmutableItem forward  = items[i == items.length - 1 ? 0 : i + 1];
            ItemWrapper.TransmutableItem backward = items[i == 0 ? items.length - 1 : i - 1];

            WorldTransmutationNode node = new WorldTransmutationNode(orijin, forward, backward);
            this.registered.add(node);
        }

        return this;
    }

    public ArrayList<WorldTransmutationNode> getRegistered() {
        return this.registered;
    }
}
