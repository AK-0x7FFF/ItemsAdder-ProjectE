package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.ItemWrapper;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

public class EMCManager {
    private final ProjectE plugin;

    private final NamespacedKey emcKey;
    private Object2ObjectOpenHashMap<ItemWrapper.TransmutableItem, BigInteger> emcValues;
    private final Object2ObjectMap<UUID, BigInteger> playersEMCMap;

    public EMCManager(ProjectE plugin) {
        this.plugin = plugin;

        this.emcKey = new NamespacedKey(plugin, "emc");
        this.emcValues = new Object2ObjectOpenHashMap<>();
        this.emcValues.defaultReturnValue(BigInteger.ZERO);
        this.playersEMCMap = new Object2ObjectOpenHashMap<>();
        this.playersEMCMap.defaultReturnValue(null);

        // 定時儲存
        Bukkit.getScheduler().runTaskTimer(plugin, this::saveAllPlayerEMCMap2PDC, 6000L, 6000L);
    }

    public void build(WorldTransmutationsBuilder worldTransmutationsBuilder) throws FileNotFoundException {
        EMCBuilder builder = new EMCBuilder(plugin);
        this.emcValues = builder.build(worldTransmutationsBuilder);
    }

    public BigInteger getItemEMC(ItemStack item) {
        ItemWrapper.TransmutableItem itemWrapped = ItemWrapper.of(item);
        BigInteger emc = this.emcValues.get(itemWrapped);

        if (emc == null || emc.compareTo(BigInteger.ZERO) <= 0)
            return BigInteger.ZERO;

        if (itemWrapped instanceof ItemWrapper.ExactItem) {
            // 取 Material 的 EMC
            ItemWrapper.MaterialItem material = ItemWrapper.toMaterialItem(itemWrapped);
            emc = this.emcValues.get(material);
        }

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

    public BigInteger getItemEMCTotal(ItemStack item) {
        return this.getItemEMC(item).multiply(BigInteger.valueOf(item.getAmount()));
    }

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

    public void setPlayerEMC(Player player, BigInteger value) {
        this.setPlayerEMC(player.getUniqueId(), value);
    }

    public void setPlayerEMC(UUID uuid, BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) < 0)
            value = BigInteger.ZERO;

        this.playersEMCMap.put(uuid, value);
    }

    public void addPlayerEMC(Player player, BigInteger value) {
        this.addPlayerEMC(player.getUniqueId(), value);
    }

    public void addPlayerEMC(UUID uuid, BigInteger value) {
        BigInteger newValue = getPlayerEMC(uuid).add(value);
        if (newValue.compareTo(BigInteger.ZERO) < 0)
            newValue = BigInteger.ZERO;

        setPlayerEMC(uuid, newValue);
    }

    public void savePlayerEMCMap2PDC(Player player) {
        this.savePlayerEMCMap2PDC(player.getUniqueId());
    }

    public void savePlayerEMCMap2PDC(UUID uuid) {
        BigInteger value = this.getPlayerEMC(uuid);

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            this.plugin.logger.warning("FAILED Save EMC to PDC: " + uuid);
            return;
        }

        player.getPersistentDataContainer().set(this.emcKey, PersistentDataType.STRING, value.toString());
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
