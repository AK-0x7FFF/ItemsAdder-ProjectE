package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.ItemWrapper;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.*;
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
    }

    private String item2NamespacedID(ItemStack item) {
        String namespacedID = null;
        Object wrappered = ItemWrapper.of(item);

        switch (wrappered) {
            case Material              material  -> namespacedID = material.getKey().toString();
            case ItemWrapper.ExactItem exactItem -> namespacedID = exactItem.getMaterial().getKey().toString();
            case ItemWrapper.IAItem    iaItem    -> namespacedID = iaItem.getNamespacedID();
            default -> {}
        }

        return namespacedID;
    }

    @Nullable
    private ItemStack namespacedID2Item(String namespacedID) {
        CustomStack iaStack = CustomStack.getInstance(namespacedID);
        if (iaStack != null) {
            return iaStack.getItemStack();
        }

        NamespacedKey key = NamespacedKey.fromString(namespacedID);
        if (key != null && key.getNamespace().equals(NamespacedKey.MINECRAFT_NAMESPACE)) {
            Material mat = Material.getMaterial(key.getKey().toUpperCase());
            if (mat != null)
                return new ItemStack(mat);
        }

        return null;
    }

    public boolean isLearned(Player player, ItemStack item) {
        return this.isLearned(player.getUniqueId(), item);
    }
    public boolean isLearned(UUID uuid, ItemStack item) {
        Set<String> knowledge = getKnowledge(uuid);
        return knowledge.contains(this.item2NamespacedID(item));
    }

    public boolean isLearnable(ItemStack item) {
        return this.plugin.getEmcManager().getItemEMC(item).compareTo(BigInteger.ZERO) > 0;
    }

    public void learn(Player player, ItemStack item) {
        this.learn(player.getUniqueId(), item);
    }
    public void learn(UUID uuid, ItemStack item) {
        if (item.getType().equals(Material.AIR))
            return;

        this.getKnowledge(uuid).add(this.item2NamespacedID(item));
    }

    public void unlearn(Player player,ItemStack item) {
        this.unlearn(player.getUniqueId(), item);
    }
    public void unlearn(UUID uuid,ItemStack item) {
        if (item.getType().equals(Material.AIR))
            return;

        this.getKnowledge(uuid).remove(this.item2NamespacedID(item));
    }

    public List<ItemStack> getKnowledgeItem(UUID uuid) {
        return getKnowledge(uuid).stream()
            .map(this::namespacedID2Item)
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

    public Set<String> getKnowledge(Player player) {
        return this.getKnowledge(player.getUniqueId());
    }
    public Set<String> getKnowledge(UUID uuid) {
        Set<String> knowledge = this.playersKnowledgeMap.get(uuid);
        if  (knowledge != null)
            return knowledge;

        knowledge = this.getKnowledgeFromPDC(uuid);
        this.playersKnowledgeMap.put(uuid, knowledge);
        return knowledge;
    }

    public Set<String> getKnowledgeFromPDC(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return new ObjectOpenHashSet<>();

        return this.getKnowledgeFromPDC(player);
    }
    public Set<String> getKnowledgeFromPDC(Player player) {
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

    public boolean saveKnowledge2PDC(UUID uuid, Set<String> knowledge) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return false;

        return this.saveKnowledge2PDC(player, knowledge);
    }
    public boolean saveKnowledge2PDC(Player player, Set<String> knowledge) {
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
        Set<String> knowledge = this.getKnowledge(uuid);
        return this.saveKnowledge2PDC(uuid, knowledge);
    }
    public boolean savePlayerKnowledgeMap2PDC(Player player) {
        Set<String> knowledge = this.getKnowledge(player);
        return this.saveKnowledge2PDC(player, knowledge);
    }

    public void saveAllPlayerKnowledgeMap2PDC() {
        for (Map.Entry<UUID, Set<String>> entry : this.playersKnowledgeMap.entrySet()) {
            UUID uuid = entry.getKey();
            Set<String> value = entry.getValue();

            this.saveKnowledge2PDC(uuid, value);
        }
    }
}
