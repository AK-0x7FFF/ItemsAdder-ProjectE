package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.utils.EMCFormatter;
import ak.ak32767.projecte.utils.ItemBase64Converter;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TransTableManager {
    private static final NamespacedKey STORAGE_KEY = new NamespacedKey("projecte", "transtable_storage");;

    private final ProjectE plugin;
    private final EMCManager emcManager;
    private final KnowledgeManager knowledgeManager;
    private final Player player;

    private List<List<ItemStack>> pages;
    private ItemStack pagesMaxEMCItem;

    public TransTableManager(ProjectE plugin, Player player) {
        this.plugin = plugin;
        this.emcManager = plugin.getEmcManager();
        this.knowledgeManager = plugin.getKnowledgeManager();

        this.player = player;
        this.pages = new ObjectArrayList<>();
        this.pagesMaxEMCItem = ItemStack.of(Material.AIR);
        this.updatePagesForce();
    }

    public EMCManager getEmcManager() {
        return emcManager;
    }

    public KnowledgeManager getKnowledgeManager() {
        return knowledgeManager;
    }

    private record ResultTakeItemCalc(BigInteger emcNeed, int itemAmount) {}

    public ResultTakeItemCalc takeItemCalc(ItemStack item, int amount) {
        BigInteger playerEMC = this.emcManager.getPlayerEMC(this.player);
        BigInteger emcNeedSingle = this.emcManager.getItemEMC(item);

        if (playerEMC.compareTo(emcNeedSingle) < 0)
            return new ResultTakeItemCalc(BigInteger.ZERO, 0);

        BigInteger emcNeed = emcNeedSingle.multiply(BigInteger.valueOf(amount));
        if (playerEMC.compareTo(emcNeed) < 0) {
            amount = playerEMC.divide(emcNeedSingle).intValue();
            emcNeed = emcNeedSingle.multiply(BigInteger.valueOf(amount));
        }

        return new ResultTakeItemCalc(emcNeed, amount);
    }

    public boolean giveItem2Inventory(ItemStack item, int amount) {
        item = item.clone();

        ResultTakeItemCalc takeItemCalc = this.takeItemCalc(item, amount);
        BigInteger emcNeed = takeItemCalc.emcNeed();
        amount = takeItemCalc.itemAmount();

        if (amount == 0)
            return false;

        item.setAmount(amount);
        Map<Integer, ItemStack> leakedItems = this.player.getInventory().addItem(item);

        // 背包溢出處理
        for (ItemStack leakedItem : leakedItems.values()) {
            int leakedCount = leakedItem.getAmount();
            emcNeed = emcNeed.subtract(this.emcManager.getItemEMC(item).multiply(BigInteger.valueOf(leakedCount)));
        }

        emcManager.addPlayerEMC(this.player, emcNeed.negate());
        return true;
    }

    public boolean giveItem2Cursor(ItemStack item, int amount, boolean isNew) {
        item = item.clone();

        ResultTakeItemCalc takeItemCalc = this.takeItemCalc(item, amount);
        BigInteger emcNeed = takeItemCalc.emcNeed();
        amount = takeItemCalc.itemAmount();

        // 餘額不足，請充值 :(
        if (amount == 0)
            return false;

        item.setAmount(isNew ? amount : item.getAmount() + amount);
        this.player.setItemOnCursor(item);
        this.player.updateInventory();

        emcManager.addPlayerEMC(this.player, emcNeed.negate());
        return true;
    }

    public boolean dropItem(ItemStack item, int amount) {
        item = item.clone();

        ResultTakeItemCalc takeItemCalc = this.takeItemCalc(item, amount);
        BigInteger emcNeed = takeItemCalc.emcNeed();
        amount = takeItemCalc.itemAmount();

        // 餘額不足，請充值 :(
        if (amount == 0)
            return false;

        item.setAmount(amount);
        this.player.getWorld().dropItemNaturally(this.player.getEyeLocation(), item);

        emcManager.addPlayerEMC(this.player, emcNeed.negate());
        return true;
    }

    public List<ItemStack> getStorageItem() {
        String b64 = this.player.getPersistentDataContainer().get(STORAGE_KEY, PersistentDataType.STRING);
        if (b64 == null)
            return new ObjectArrayList<>(Collections.nCopies(9, new ItemStack(Material.AIR)));

        List<ItemStack> storage = ItemBase64Converter.base642Item(b64);
        while (storage.size() < 9) {
            storage.add(new ItemStack(Material.AIR));
        }

        return storage;
    }

    public void saveStorageItem(List<ItemStack> storage) {
        String b64 = ItemBase64Converter.item2Base64(storage);
        this.player.getPersistentDataContainer().set(this.STORAGE_KEY, PersistentDataType.STRING, b64);
    }

    public boolean tryLearnItem(ItemStack item) {
        if (item.isEmpty())
            return false;

        if (!this.knowledgeManager.isLearnable(item)) {
            return false;
        }

        if (this.knowledgeManager.isLearned(this.player, item))
            return false;

        this.knowledgeManager.learn(this.player, item);
        this.updatePagesForce();
        return true;
    }

    public boolean transmuteItem(ItemStack item) {
        if (!this.knowledgeManager.isLearnable(item)) {
            return false;
        }

        if (this.tryLearnItem(item))
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.4f);

        this.emcManager.addPlayerEMC(player, this.emcManager.getItemEMC(item).multiply(BigInteger.valueOf(item.getAmount())));
        return true;
    }

    public boolean transmuteCursorItem(ItemStack item, int amount) {
        item = item.clone();

        if (!this.transmuteItem(item))
            return false;

        int targetItemAmount = item.getAmount() - amount;
        if (targetItemAmount == 0)
            this.player.setItemOnCursor(ItemStack.empty());
        else {
            item.setAmount(targetItemAmount);
            this.player.setItemOnCursor(item);
        }

        return true;
    }

    public boolean unlearnItem(ItemStack item) {
        item = item.clone();

        if (!this.knowledgeManager.isLearnable(item))
            return false;

        if (!this.knowledgeManager.isLearned(player, item))
            return false;

        this.knowledgeManager.unlearn(player, item);
        return true;
    }

    public void updatePagesForce() {
        BigInteger maxEMC = this.emcManager.getItemEMC(this.pagesMaxEMCItem);
        List<ItemStack> items = this.knowledgeManager.getKnowledgeItemEMCSorted(this.player);

        if (maxEMC.compareTo(BigInteger.ZERO) > 0) {
            items = new ObjectArrayList<>(
                items.stream()
                .filter(item -> this.emcManager.getItemEMC(item).compareTo(maxEMC) <= 0)
                .toList()
            );
            int index = items.indexOf(this.pagesMaxEMCItem);
            if (index > 0) {
                Collections.rotate(items.subList(0, index + 1), 1);
            }
        }
        this.pages = Lists.partition(items, 6);
    }

    public int getPagesSize() {
        return this.pages.size();
    }

    public boolean tryRefreshPages() {
        if (this.pages.isEmpty()) {
            this.updatePagesForce();
            return true;
        }

        // 萃取欄首位物品EMC 對比 玩家EMC
        BigInteger firstItemEMC = this.emcManager.getItemEMC(this.pages.getFirst().getFirst());
        if (firstItemEMC.compareTo(this.emcManager.getPlayerEMC(this.player)) < 0) {
            this.updatePagesForce();
            return true;
        }

        BigInteger maxEMC = this.emcManager.getItemEMC(this.pagesMaxEMCItem);
        if (maxEMC.compareTo(BigInteger.ZERO) == 0 || maxEMC.compareTo(firstItemEMC) < 0) {
            this.updatePagesForce();
            return true;
        }

        return false;
    }

    public ItemStack getPagesMaxEMCItem() {
        return pagesMaxEMCItem;
    }

    public void setPagesMaxEMCItem(ItemStack item) {
        this.pagesMaxEMCItem = item;
        this.tryRefreshPages();
    }

    public List<ItemStack> getPageItems(int page) {
        List<ItemStack> pageItems = this.pages.get(page);
        while (pageItems.size() < 6)
            pageItems.add(new ItemStack(Material.AIR));
        return pageItems;
    }

    public ItemStack tagEMC2Item(@NotNull ItemStack item) {
        item = item.clone();
        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return item;

        List<Component> lore = meta.hasLore() ? meta.lore() : new ObjectArrayList<>();

        BigInteger emc = this.emcManager.getItemEMC(item);
        String emcStr = EMCFormatter.commaFormat(emc);
        if (emc.compareTo(BigInteger.valueOf(1_000_000)) >= 0)
            emcStr = EMCFormatter.numberNameFormat(emc) + " (" + emcStr + ")";

        lore.addFirst(
            Component.text()
                .append(Component.text("EMC: ", NamedTextColor.YELLOW))
                .append(Component.text(emcStr, NamedTextColor.WHITE))
                .decoration(TextDecoration.ITALIC, false)
                .build()
        );

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}