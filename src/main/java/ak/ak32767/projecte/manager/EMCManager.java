package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EMCManager {
    private final ProjectE plugin;

    private final NamespacedKey emcKey;
    private final Object2ObjectOpenHashMap<ItemWrapper.TransmutableItem, BigInteger> emcValues;
    private final Object2ObjectMap<UUID, BigInteger> playersEMCMap;

    // DEBUG
    private final Object2ObjectOpenHashMap<ItemWrapper.TransmutableItem, List<EMCBuilder.ItemEMCCalcStep>> emcCalcLogger;

    public EMCManager(ProjectE plugin) {
        this.plugin = plugin;

        this.emcKey = new NamespacedKey(plugin, "emc");
        this.emcValues = new Object2ObjectOpenHashMap<>();
        this.emcValues.defaultReturnValue(BigInteger.ZERO);
        this.playersEMCMap = new Object2ObjectOpenHashMap<>();
        this.playersEMCMap.defaultReturnValue(null);

        // DEBUG
        this.emcCalcLogger = new Object2ObjectOpenHashMap<>();
        this.emcCalcLogger.defaultReturnValue(new ObjectArrayList<>());

        // 定時儲存
        Bukkit.getScheduler().runTaskTimer(plugin, this::saveAllPlayerEMCMap2PDC, 6000L, 6000L);
    }

    public void build() {
        EMCBuilder builder = new EMCBuilder(plugin);
        this.emcValues.clear();
        this.emcValues.putAll(builder.build());
        this.emcCalcLogger.clear();
        this.emcCalcLogger.putAll(builder.getEMCCalcLogger());
    }

    public List<String> getItemEMCCalcLog(ItemStack item) {
        List<String> output = new ObjectArrayList<>();

        if (item == null || item.isEmpty())
            return output;

        var itemWrapped = ItemWrapper.of(item);
        var steps = this.emcCalcLogger.get(itemWrapped);

        for (var step : steps) {
            output.add(step.prev() + " -> " + step.now());
            for (var snap : step.itemEMCSnap()) {
                output.add("    +" + snap.emc() + " (x" + snap.amount() + "): " + snap.item());
            }
        }

        return output;
    }


    public boolean isTransmutable(ItemStack item) {
        return this.isTransmutable(item, false);
    }
    public boolean isTransmutable(ItemStack item, boolean exact) {
        return this.getItemEMC(item, exact).compareTo(BigInteger.ZERO) > 0;
    }

    public BigInteger getItemEMC(ItemStack item) {
        return this.getItemEMC(item, false);
    }
    public BigInteger getItemEMC(ItemStack item, boolean exact) {
        if (item == null || item.isEmpty())
            return BigInteger.ZERO;

        ItemWrapper.TransmutableItem itemWrapped = ItemWrapper.of(item);
        BigInteger emc = this.emcValues.get(itemWrapped);

        if (
            !exact &&
            ItemWrapper.Transmutable.EXACT_ITEM.is(itemWrapped) &&
            (emc == null || emc.compareTo(BigInteger.ZERO) <= 0)
        ) {
            // 取 Material 的 EMC
            ItemWrapper.MaterialItem material = ItemWrapper.toMaterialItemWrapper(itemWrapped);
            emc = this.emcValues.get(material);
        }

        if (emc == null || emc.compareTo(BigInteger.ZERO) <= 0)
            return BigInteger.ZERO;

        if (!item.hasItemMeta())
            return emc;

        ItemMeta meta = item.getItemMeta();
        Damageable damageableMeta = (Damageable) meta;
        if (!damageableMeta.hasDamageValue())
            return emc;

        int max = item.getType().getMaxDurability();
        if (damageableMeta.hasMaxDamage())
            max = damageableMeta.getMaxDamage();

        // 等比扣除耐久 EMC
        emc = emc.multiply(BigInteger.valueOf(max - damageableMeta.getDamage())).divide(BigInteger.valueOf(max));

        return emc;
    }

    public Map<ItemWrapper.TransmutableItem, BigInteger> getAllItemsEMC() {
        return this.emcValues;
    }

//    public BigInteger getItemEMCTotal(ItemStack item) {
//        return this.getItemEMC(item).multiply(BigInteger.valueOf(item.getAmount()));
//    }

    public BigInteger getPlayerEMC(Player player) {
        return this.getPlayerEMC(player.getUniqueId());
    }
    public BigInteger getPlayerEMC(UUID uuid) {
        BigInteger value = this.playersEMCMap.get(uuid);
        if (value != null)
            return value;

        Player player = this.plugin.getServer().getPlayer(uuid);
        if (player == null) {
//            player = Bukkit.getOfflinePlayer(uuid).getPlayer();
            return BigInteger.ZERO;
        }
        String valueRaw = player.getPersistentDataContainer().get(this.emcKey, PersistentDataType.STRING);
        value = valueRaw == null ? BigInteger.ZERO : new BigInteger(valueRaw);

        this.playersEMCMap.put(uuid, value);

        return value;
    }

    public BigInteger setPlayerEMC(Player player, BigInteger value) {
        return this.setPlayerEMC(player.getUniqueId(), value);
    }
    public BigInteger setPlayerEMC(UUID uuid, BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) < 0)
            value = BigInteger.ZERO;

        this.playersEMCMap.put(uuid, value);
        return value;
    }

    public BigInteger addPlayerEMC(Player player, BigInteger value) {
        return this.addPlayerEMC(player.getUniqueId(), value);
    }
    public BigInteger addPlayerEMC(UUID uuid, BigInteger value) {
        BigInteger newValue = getPlayerEMC(uuid).add(value);
        if (newValue.compareTo(BigInteger.ZERO) < 0)
            newValue = BigInteger.ZERO;

        return setPlayerEMC(uuid, newValue);
    }

    public boolean savePlayerEMCMap2PDC(Player player) {
        return this.savePlayerEMCMap2PDC(player.getUniqueId());
    }
    public boolean savePlayerEMCMap2PDC(UUID uuid) {
        BigInteger value = this.getPlayerEMC(uuid);

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            this.plugin.logger.warning("FAILED Save EMC to PDC: " + uuid);
            return false;
        }

        player.getPersistentDataContainer().set(this.emcKey, PersistentDataType.STRING, value.toString());
        return true;
    }

    public void saveAllPlayerEMCMap2PDC() {
        for (Map.Entry<UUID, BigInteger> entry : this.playersEMCMap.entrySet()) {
            UUID uuid = entry.getKey();
            BigInteger value = entry.getValue();

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                this.plugin.logger.warning("FAILED Save EMC to PDC: " + uuid);
                continue;
            }

            player.getPersistentDataContainer().set(this.emcKey, PersistentDataType.STRING, value.toString());
        }
    }

    public void unloadPlayerOnEMCMap(Player player) {
        this.unloadPlayerOnEMCMap(player.getUniqueId());
    }
    public void unloadPlayerOnEMCMap(UUID uuid) {
        this.playersEMCMap.remove(uuid);
    }
}
