package ak.ak32767.projecte.emcsys;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.ProjectEException;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.utils.YAMLLoader;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhiloCraftTransmutationBuilder {
    public record PhiloCraftTransmutationData(
        ItemWrapper.TransmutableItem itemA, long amountA,
        ItemWrapper.TransmutableItem itemB, long amountB
    ) {}

    private final ProjectE plugin;
    private final Set<PhiloCraftTransmutationData> conversions;

    public PhiloCraftTransmutationBuilder(ProjectE plugin) {
        this.plugin = plugin;
        this.conversions = new ObjectLinkedOpenHashSet<>();
    }

    public Set<PhiloCraftTransmutationData> getConversions() {
        return this.conversions;
    }

    public boolean build() {
        try {
            FileConfiguration config = YAMLLoader.load(this.plugin, "data/transmutation_data.yml");
            buildByYAML(config.getMapList("philocraft"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void buildByYAML(List<Map<?, ?>> transList) throws ProjectEException.YAMLKeyOrValueErrorException {
        for (Map<?, ?> entry : transList) {
            Map<?, ?> itemARaw = (Map<?, ?>) entry.get("item_a");
            Map<?, ?> itemBRaw = (Map<?, ?>) entry.get("item_b");
            if (itemARaw == null || itemBRaw == null)
                throw new ProjectEException.YAMLKeyOrValueErrorException();

            ItemWrapper.TransmutableItem itemA = YAMLLoader.ItemYAMLWrapper.of(itemARaw).getFirst();
            ItemWrapper.TransmutableItem itemB = YAMLLoader.ItemYAMLWrapper.of(itemBRaw).getFirst();

            long itemAAmount = (itemARaw.get("amount") instanceof Number num) ? num.longValue() : 0;
            long itemBAmount = (itemBRaw.get("amount") instanceof Number num) ? num.longValue() : 0;
            if (itemAAmount < 1 || itemAAmount > 8 || itemBAmount < 1 || itemBAmount > 8)
                throw new ProjectEException.YAMLKeyOrValueErrorException();

            this.registerConversion(itemA, itemAAmount, itemB, itemBAmount);
        }
    }

    public PhiloCraftTransmutationBuilder registerConversion(
        ItemWrapper.TransmutableItem itemA, long amountA,
        ItemWrapper.TransmutableItem itemB, long amountB
    ) {
        this.conversions.add(new PhiloCraftTransmutationData(itemA, amountA, itemB, amountB));
        return this;
    }
}
