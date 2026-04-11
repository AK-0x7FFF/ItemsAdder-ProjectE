package ak.ak32767.projecte.utils;

import ak.ak32767.projecte.ProjectEException;
import ak.ak32767.projecte.data.ItemWrapper;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class YAMLLoader {
    public static FileConfiguration load(Plugin plugin, String location) throws FileNotFoundException {
        InputStream fileStream = plugin.getResource(location);
        if  (fileStream == null)
            throw new FileNotFoundException(location + " file not found!");

        InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
        return YamlConfiguration.loadConfiguration(reader);
    }

    public record ItemYAMLWrapper(String name, String type) {
        public static List<ItemWrapper.TransmutableItem> of(Map<?, ?> entry) {
            if (entry == null)
                return null;

            if (!entry.containsKey("name") || !entry.containsKey("type"))
                return null;

            String name = String.valueOf(entry.get("name")).toUpperCase();
            String type = String.valueOf(entry.get("type")).toUpperCase();
            return new ItemYAMLWrapper(name, type).get();
        }

        public List<ItemWrapper.TransmutableItem> get() {
            if (name == null || type == null)
                return new ObjectArrayList<>();

            return switch (type) {
                case "MATERIAL" -> {
                    Material material = Material.getMaterial(name);
                    if (material == null)
                        yield new ObjectArrayList<>();

                    yield ObjectArrayList.of(new ItemWrapper.MaterialItem(material));
                }
                case "IAITEM" -> {
                    CustomStack instance = CustomStack.getInstance(name);
                    if (instance == null)
                        yield new ObjectArrayList<>();

                    yield ObjectArrayList.of(new ItemWrapper.IAItem(instance.getItemStack()));
                }
                case "TAG" -> {
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
                        } catch (Exception ignored) {
                            throw new ProjectEException.YAMLTagNotFoundException();
                        }
                    }

                    yield tag.getValues().stream()
                        .map(mat -> (ItemWrapper.TransmutableItem) new ItemWrapper.MaterialItem(mat))
                        .toList();
                }

                default -> new ObjectArrayList<>();
            };
        }
    };
}
