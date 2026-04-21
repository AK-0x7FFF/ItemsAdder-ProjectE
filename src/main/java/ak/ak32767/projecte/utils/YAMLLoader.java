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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class YAMLLoader {
    public static FileConfiguration loadResource(Plugin plugin, String location) throws FileNotFoundException {
        InputStream fileStream = plugin.getResource(location);

        if (fileStream == null)
            throw new FileNotFoundException(location + " file not found!");

        InputStreamReader reader = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
        return YamlConfiguration.loadConfiguration(reader);
    }

    public static FileConfiguration loadConfig(Plugin plugin, String location) throws FileNotFoundException {
        File file = new File(plugin.getDataFolder(), location);

        if (!file.exists())
            throw new FileNotFoundException(location + " file not found!");

        return YamlConfiguration.loadConfiguration(file);
    }



//    private static FileConfiguration load(InputStream fileStream) {
//
//    }

    public record ItemYAMLWrapper(String name, String type, @Nullable String meta) {
        public static List<ItemWrapper.TransmutableItem> of(Map<?, ?> entry) throws ProjectEException.YAMLKeyOrValueErrorException {
            if (entry == null)
                throw new ProjectEException.YAMLKeyOrValueErrorException("Entry is null");

            if (!entry.containsKey("name") || !entry.containsKey("type"))
                throw new ProjectEException.YAMLKeyOrValueErrorException();

            String name = String.valueOf(entry.get("name")).toUpperCase();
            String type = String.valueOf(entry.get("type")).toUpperCase();

            String meta = null;
            if (entry.containsKey("meta"))
                meta = (String) entry.get("meta");

            return new ItemYAMLWrapper(name, type, meta).get();
        }

        private List<ItemWrapper.TransmutableItem> get() throws ProjectEException.YAMLKeyOrValueErrorException {
            if (name == null || type == null)
                throw new ProjectEException.YAMLKeyOrValueErrorException("type is null");

            return switch (type) {
                case "MATERIAL" -> {
                    Material material = Material.getMaterial(name);
                    if (material == null)
                        throw new ProjectEException.YAMLKeyOrValueErrorException("type is null");

                    yield ObjectArrayList.of(new ItemWrapper.MaterialItem(material));
                }
                case "EXACTITEM" -> {
                    if (meta == null)
                        throw new ProjectEException.YAMLKeyOrValueErrorException("meta is null");

                    Material material = Material.getMaterial(name);
                    if (material == null)
                        throw new ProjectEException.YAMLKeyOrValueErrorException("type is null");

                    ItemStack item;
                    try {
                        String input = material.getKey().getNamespace() + ":" + material.getKey().getKey() + meta;
                        item = Bukkit.getItemFactory().createItemStack(input);
                    } catch (Exception e) {
                        throw new ProjectEException.YAMLKeyOrValueErrorException();
                    }

                    yield ObjectArrayList.of(new ItemWrapper.ExactItem(item));
                }
                case "IAITEM" -> {
                    CustomStack instance = CustomStack.getInstance(name.toLowerCase());
                    if (instance == null)
                        throw new ProjectEException.YAMLKeyOrValueErrorException("type is null");

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
                            throw new ProjectEException.YAMLKeyOrValueErrorException("type is null");
                        }
                    }

                    yield tag.getValues().stream()
                        .map(mat -> (ItemWrapper.TransmutableItem) new ItemWrapper.MaterialItem(mat))
                        .toList();
                }

                default -> throw new ProjectEException.YAMLKeyOrValueErrorException("type is null");
            };
        }
    };
}
