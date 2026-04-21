package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.utils.ItemBase64Converter;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class KnowledgeManager {
    private final ProjectE plugin;
    private final NamespacedKey namespacedKey;
    private final Object2ObjectMap<UUID, Set<String>> playersKnowledgeMap;

    public KnowledgeManager(ProjectE plugin) {
        this.plugin = plugin;

        this.namespacedKey = new NamespacedKey(plugin, "knowledge");
        this.playersKnowledgeMap = new Object2ObjectOpenHashMap<>();
        this.playersKnowledgeMap.defaultReturnValue(null);

        Bukkit.getScheduler().runTaskTimer(plugin, this::saveAllPlayerKnowledgeMap2PDC, 6000L, 6000L);
    }

    public @Nullable String item2KnowledgeFormat(ItemStack item) {
        Object wrappered = ItemWrapper.of(item);

        return switch (wrappered) {
            case ItemWrapper.MaterialItem material ->
                material.material().getKey().toString();
            case ItemWrapper.ExactItem exactItem ->
                exactItem.material().getKey() + ">" + ItemBase64Converter.meta2Base64(exactItem.meta());
            case ItemWrapper.IAItem iaItem ->
                iaItem.namespacedID();
            default -> null;
        };
    }

    public @Nullable ItemStack knowledgeFormat2Item(String knowledge) {
        if (knowledge.contains(">")) {
            String[] a = knowledge.split(">");

            NamespacedKey key = NamespacedKey.fromString(a[0]);
            if (key == null)
                return null;

            Material material = Material.getMaterial(key.getKey().toUpperCase());
            if (material == null)
                return null;

            ItemMeta meta = ItemBase64Converter.base642Meta(a[1]);
            if (meta == null)
                return null;

            return new ItemWrapper.ExactItem(material, meta).item();
        }

        CustomStack iaItem = CustomStack.getInstance(knowledge);
        if (iaItem != null) {
            return iaItem.getItemStack();
        }

        NamespacedKey key = NamespacedKey.fromString(knowledge);
        if (key == null)
            return null;

        Material material = Material.getMaterial(key.getKey().toUpperCase());
        if (material != null)
            return new ItemStack(material);

        return null;
    }

    public boolean isLearned(Player player, ItemStack item) {
        return this.isLearned(player.getUniqueId(), item);
    }
    public boolean isLearned(UUID uuid, ItemStack item) {
        Set<String> knowledge = getPlayerKnowledge(uuid);
        return knowledge.contains(this.item2KnowledgeFormat(item));
    }

    public boolean isLearnable(ItemStack item) {
        return this.plugin.getEmcManager()
            .getItemEMC(item, true)
            .compareTo(BigInteger.ZERO) > 0;
    }

    public void learn(Player player, ItemStack item) {
        this.learn(player.getUniqueId(), item);
    }
    public void learn(UUID uuid, ItemStack item) {
        if (item.isEmpty())
            return;

        this.getPlayerKnowledge(uuid).add(this.item2KnowledgeFormat(item));
    }

    public void unlearn(Player player, ItemStack item) {
        this.unlearn(player.getUniqueId(), item);
    }
    public void unlearn(UUID uuid, ItemStack item) {
        if (item.isEmpty())
            return;

        this.getPlayerKnowledge(uuid).remove(this.item2KnowledgeFormat(item));
    }

    public List<ItemStack> getKnowledgeItem(UUID uuid) {
        return getPlayerKnowledge(uuid).stream()
            .map(this::knowledgeFormat2Item)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    public List<ItemStack> getKnowledgeItemEMCSorted(Player player) {
        return this.getKnowledgeItemEMCSorted(player.getUniqueId());
    }
    public List<ItemStack> getKnowledgeItemEMCSorted(UUID uuid) {
        List<ItemStack> items = this.getKnowledgeItem(uuid);

        Object2ObjectMap<ItemStack, BigInteger> emcCache = new Object2ObjectOpenHashMap<>(items.size());
        for (ItemStack item : items)
            emcCache.put(item, this.plugin.getEmcManager().getItemEMC(item));
        items.sort((a, b) -> emcCache.get(b).compareTo(emcCache.get(a)));

        return items;
    }

    public Set<String> getPlayerKnowledge(Player player) {
        return this.getPlayerKnowledge(player.getUniqueId());
    }
    public Set<String> getPlayerKnowledge(UUID uuid) {
        Set<String> knowledge = this.playersKnowledgeMap.get(uuid);
        if  (knowledge != null)
            return knowledge;

        knowledge = this.getPlayerKnowledgeFromPDC(uuid);
        this.playersKnowledgeMap.put(uuid, knowledge);
        return knowledge;
    }

    public Set<String> getPlayerKnowledgeFromPDC(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return new ObjectOpenHashSet<>();

        return this.getPlayerKnowledgeFromPDC(player);
    }
    public Set<String> getPlayerKnowledgeFromPDC(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        if (!pdc.has(this.namespacedKey, PersistentDataType.BYTE_ARRAY))
            return new ObjectOpenHashSet<>();

        byte[] compressed = pdc.get(this.namespacedKey, PersistentDataType.BYTE_ARRAY);
        if (compressed == null || compressed.length == 0)
            return new ObjectOpenHashSet<>();

        try (GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(compressed))){
            String raw = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            if (raw.isEmpty())
                return new ObjectOpenHashSet<>();
            return new ObjectOpenHashSet<>(Arrays.asList(raw.split(",")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ObjectOpenHashSet<>();
    }

    public boolean savePlayerKnowledge2PDC(UUID uuid, Set<String> knowledge) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return false;

        return this.savePlayerKnowledge2PDC(player, knowledge);
    }
    public boolean savePlayerKnowledge2PDC(Player player, Set<String> knowledge) {
        byte[] compressed = new byte[0];

        String raw = String.join(",", knowledge);
        if (!raw.isEmpty()) {
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
                gzip.write(raw.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            compressed = obj.toByteArray();
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(this.namespacedKey, PersistentDataType.BYTE_ARRAY, compressed);

        return true;
    }

    public boolean savePlayerKnowledgeMap2PDC(UUID uuid) {
        Set<String> knowledge = this.getPlayerKnowledge(uuid);
        return this.savePlayerKnowledge2PDC(uuid, knowledge);
    }
    public boolean savePlayerKnowledgeMap2PDC(Player player) {
        Set<String> knowledge = this.getPlayerKnowledge(player);
        return this.savePlayerKnowledge2PDC(player, knowledge);
    }

    public void saveAllPlayerKnowledgeMap2PDC() {
        for (Map.Entry<UUID, Set<String>> entry : this.playersKnowledgeMap.entrySet()) {
            UUID uuid = entry.getKey();
            Set<String> value = entry.getValue();

            this.savePlayerKnowledge2PDC(uuid, value);
        }
    }

    public List<String> getAllLearnableItem() {
        return this.plugin.getEmcManager().getAllItemsEMC().keySet().stream()
            .map(item -> this.item2KnowledgeFormat(item.item()))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ObjectArrayList::new));
    }
}
