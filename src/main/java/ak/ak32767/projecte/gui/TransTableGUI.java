package ak.ak32767.projecte.gui;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.manager.TransTableManager;
import ak.ak32767.projecte.utils.EMCFormatter;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;


public class TransTableGUI extends GUIBase {
    private static final FontImageWrapper BACKGROUND = new FontImageWrapper("projecte:transmute");

    private static final IntList STORAGE_SLOTS = new IntArrayList(new int[] {2, 9, 10, 11, 19, 20, 21, 28});
    private static final IntList EXTRACTION_OUTER_RING_SLOTS = new IntArrayList(new int[] {5, 7, 17, 35, 43, 41});
    private static final IntList EXTRACTION_INNER_RING_SLOTS = new IntArrayList(new int[] {15, 25, 33, 23});
    private static final int EMC_FILTER_SLOT = 24;
    private static final int EMC_CHECKER_SLOT = 46;
    private static final int UNLEARN_SLOT = 47;
    private static final int TRANSMUTE_SLOT = 48;
    private static final int PREV_PAGE_SLOT = 50;
    private static final int SEARCH_SLOT = 51;
    private static final int NEXT_PAGE_SLOT = 52;
    private static final Set<Integer> ALLOWED_SLOTS = new HashSet<>(); static {
        ALLOWED_SLOTS.add(EMC_FILTER_SLOT);
        ALLOWED_SLOTS.add(UNLEARN_SLOT);
        ALLOWED_SLOTS.add(TRANSMUTE_SLOT);
        ALLOWED_SLOTS.add(PREV_PAGE_SLOT);
        ALLOWED_SLOTS.add(SEARCH_SLOT);
        ALLOWED_SLOTS.add(NEXT_PAGE_SLOT);
        ALLOWED_SLOTS.addAll(STORAGE_SLOTS);
        ALLOWED_SLOTS.addAll(EXTRACTION_OUTER_RING_SLOTS);
        ALLOWED_SLOTS.addAll(EXTRACTION_INNER_RING_SLOTS);
    };

    private Inventory inventory;

    private TransTableManager transTableManager;
    private ItemStack emcCheckerItem;
    private Map<ItemStack, ItemStack> emcTaggedItemMap;
    private int page;

    public TransTableGUI(ProjectE plugin, Player player) {
        super(plugin, player);
    }

    public static class MyHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() { return null; }
    }

    @Override
    public void setupGUI(ProjectE plugin, Player player) {
        // GUI 初始化
        TexturedInventoryWrapper inventoryWrapper = new TexturedInventoryWrapper(new MyHolder(), 54, null, 0, -16, BACKGROUND);
        this.inventory = inventoryWrapper.getInternal();

        this.transTableManager = new TransTableManager(plugin, player);
        this.emcTaggedItemMap = new Object2ObjectOpenHashMap<>();

        // 欄位初始化
        this.emcCheckerItem = ItemStack.of(Material.PLAYER_HEAD); {
            SkullMeta playerHeadMeta = (SkullMeta) emcCheckerItem.getItemMeta();
            playerHeadMeta.setOwningPlayer(player);
            emcCheckerItem.setItemMeta(playerHeadMeta);
        };
        this.inventory.setItem(PREV_PAGE_SLOT, (CustomStack.getInstance("_iainternal:icon_left_blue").getItemStack()));
        this.inventory.setItem(SEARCH_SLOT, (CustomStack.getInstance("_iainternal:icon_search").getItemStack()));
        this.inventory.setItem(NEXT_PAGE_SLOT, (CustomStack.getInstance("_iainternal:icon_right_blue").getItemStack()));

        // 儲物欄
        List<ItemStack> storage = this.transTableManager.getStorageItem();
        for (int i = 0; i < STORAGE_SLOTS.size(); ++i) {
            this.inventory.setItem(STORAGE_SLOTS.getInt(i), storage.get(i));
        }
        // EMC過濾欄
        ItemStack pagesMaxEMCItem = storage.get(8);
        this.transTableManager.setPagesMaxEMCItem(pagesMaxEMCItem);
        this.inventory.setItem(EMC_FILTER_SLOT, this.transTableManager.tagEMC2Item(pagesMaxEMCItem));
        // 轉化欄
        this.page = 0;
        this.updateExtractionRing();

        this.updateEMC();
        inventoryWrapper.showInventory(player);
    }


    @Override
    protected void onStorageClick(InventoryClickEvent event) {
        event.setCancelled(true);

        int slot = event.getSlot();
        if (!ALLOWED_SLOTS.contains(slot))
            return;

        ClickType clickType = event.getClick();
        ItemStack currItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        {
            String curr = null;
            if (currItem != null) {
                curr = currItem.getType().name();
            }
            plugin.logger.info(curr + " " + cursorItem.getType().name());
        }

        // 左側儲存欄
        if (STORAGE_SLOTS.contains(slot)) {
            if (clickType == ClickType.DOUBLE_CLICK)
                return;

            if (this.transTableManager.tryLearnItem(cursorItem))
                this.player.playSound(this.player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.4f);
            this.updateExtractionRing();

            // 電池充電邏輯， 待補充
            event.setCancelled(false);

        // 右側提取欄
        } else if (EXTRACTION_INNER_RING_SLOTS.contains(slot) || EXTRACTION_OUTER_RING_SLOTS.contains(slot)) {
            if (currItem == null || currItem.isEmpty())
                return;

            ItemStack givenItem = this.emcTaggedItemMap.get(currItem);
            if (
                clickType == ClickType.LEFT || clickType == ClickType.RIGHT ||
                clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT ||
                clickType == ClickType.DOUBLE_CLICK
            ) {
                int giveAmount = 1;
                if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                        if (this.transTableManager.giveItem2Inventory(givenItem, givenItem.getMaxStackSize())) {
                            this.updateEMC();
                            this.player.playSound(this.player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                            return;
                        }

                        this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
                        return;
                }

                // 游標物品與提取物品不一致
                if (!cursorItem.isEmpty() && !cursorItem.isSimilar(givenItem))
                    return;

                int cursorItemAmount = 0;
                if (!cursorItem.isEmpty()) {
                    givenItem = cursorItem;
                    cursorItemAmount = cursorItem.getAmount();
                }

                if (cursorItemAmount + giveAmount > givenItem.getMaxStackSize())
                    return;

                if (this.transTableManager.giveItem2Cursor(givenItem, giveAmount, cursorItem.isEmpty()))
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                else
                    this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);


            // KOBE
            } else if (clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP) {
                if (!cursorItem.isEmpty())
                    return;

                int dropAmount = 1;
                if (clickType == ClickType.CONTROL_DROP)
                    dropAmount = givenItem.getMaxStackSize();

                if (this.transTableManager.dropItem(givenItem, dropAmount))
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.2f);
                else
                    this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
            }

            this.updateEMC();

        // 轉化欄
        } else if (slot == TRANSMUTE_SLOT) {
            if (cursorItem.isEmpty())
                return;

            if (clickType == ClickType.LEFT)
                if (this.transTableManager.transmuteCursorItem(cursorItem, cursorItem.getAmount()))
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_GENERIC_BURN, 0.25f, 1f);
                else
                    this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
            else if (clickType == ClickType.RIGHT)
                if (this.transTableManager.transmuteCursorItem(cursorItem, 1))
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_GENERIC_BURN, 0.25f, 1f);
                else
                    this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
            this.updateEMC();

        // 移除知識欄
        } else if (slot == UNLEARN_SLOT) {
            if (cursorItem.isEmpty())
                return;

            if (!this.transTableManager.unlearnItem(cursorItem)) {
                this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
                return;
            }
            this.transTableManager.updatePagesForce();
            this.updateExtractionRing();
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 1.2f);

        // EMC 過濾欄
        } else if (slot == EMC_FILTER_SLOT) {
//            if (clickType == ClickType.DOUBLE_CLICK)
//                return;
            if (!clickType.isMouseClick())
                return;

            if ((currItem == null || currItem.isEmpty()) && cursorItem.isEmpty())
                return;

            if (!cursorItem.isEmpty()) {
                if (this.transTableManager.getKnowledgeManager().isLearnable(cursorItem)) {
                    this.swapCursorItem2Filter(cursorItem);
                    return;
                }
                this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
                return;
            }

            this.filterItem2Cursor();

        // 翻頁的
        } else if (slot == PREV_PAGE_SLOT) {
            if (clickType == ClickType.DOUBLE_CLICK)
                return;
            this.prevPage();
        } else if (slot == NEXT_PAGE_SLOT) {
            if (clickType == ClickType.DOUBLE_CLICK)
                return;
            this.nextPage();
        }
    }

    @Override
    protected void onPlayerInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ClickType clickType = event.getClick();
        ItemStack currItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        switch (clickType) {
            case LEFT: case RIGHT: case MIDDLE:
            case NUMBER_KEY: case DROP: case CONTROL_DROP: case SWAP_OFFHAND:
                event.setCancelled(false);
                break;

            case SHIFT_LEFT: case SHIFT_RIGHT:
//                this.transTableManager.getEmcManager().addPlayerEMC(player, this.transTableManager.getEmcManager().getItemEMCTotal(currItem));
                if (this.transTableManager.transmuteItem(currItem))
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_GENERIC_BURN, 0.25f, 1f);
                else {
                    this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
                    return;
                }
                event.setCurrentItem(ItemStack.empty());

                this.updateEMC();
                this.updateExtractionRing();
                break;
        }
    }

    @Override
    public void onInventoryClosePost(InventoryCloseEvent event) {
        List<ItemStack> storage = new ArrayList<>();
        for (int slot : STORAGE_SLOTS) {
            ItemStack item = this.inventory.getItem(slot);
            if (item == null)
                item = new ItemStack(Material.AIR);

            storage.add(item);
        }
        storage.add(this.transTableManager.getPagesMaxEMCItem());

        this.transTableManager.saveStorageItem(storage);
    }

    private void updateEMC() {
        SkullMeta itemMeta = (SkullMeta) this.emcCheckerItem.getItemMeta();
        List<Component> lore = new ObjectArrayList<>(
            new Component[]{Component.text("Detail: ", NamedTextColor.GRAY)}
        );

        BigInteger emc = this.plugin.getEmcManager().getPlayerEMC(this.player);

        String emcStr = EMCFormatter.commaFormat(emc);
        if (emc.compareTo(BigInteger.valueOf(1_000_000)) >= 0) {
            String[] emcStrSplit = emcStr.split(",");
            StringBuilder emcStrPart = new StringBuilder();

            for (int i = emcStrSplit.length - 1; i >= 0; --i) {
                if (!emcStrPart.isEmpty())
                    emcStrPart.insert(0, ",");
                emcStrPart.insert(0, emcStrSplit[i]);

                if ((emcStrSplit.length - i) % 9 == 0 || i == 0) {
                    lore.add(1, Component.text(emcStrPart.toString(), NamedTextColor.GRAY));
                    emcStrPart.setLength(0);
                }
            }

            emcStr = EMCFormatter.numberNameFormat(emc);
        }
        itemMeta.displayName(
            Component.text()
            .append(Component.text(this.player.getName() + "'s EMC: ", NamedTextColor.YELLOW))
            .append(Component.text(emcStr, NamedTextColor.WHITE))
            .decoration(TextDecoration.ITALIC, false)
            .build()
        );

        itemMeta.lore(lore);
        this.emcCheckerItem.setItemMeta(itemMeta);
        this.inventory.setItem(EMC_CHECKER_SLOT, this.emcCheckerItem);

        boolean isPageUpdated = this.transTableManager.tryRefreshPages();
        if (isPageUpdated)
            this.updateExtractionRing();
    }

    private void updateExtractionRing() {
        List<ItemStack> items = this.transTableManager.getPageItems(this.page);
        this.emcTaggedItemMap.clear();

        for (int i = 0; i < EXTRACTION_OUTER_RING_SLOTS.size(); ++i) {
            ItemStack item = items.get(i);
            ItemStack itemTagged = this.transTableManager.tagEMC2Item(item);

            this.emcTaggedItemMap.put(itemTagged, item);
            this.inventory.setItem(EXTRACTION_OUTER_RING_SLOTS.getInt(i), itemTagged);
        }
    }

    private void swapCursorItem2Filter(ItemStack cursorItem) {
        this.page = 0;
        ItemStack prevPagesMaxEMCItem = this.transTableManager.getPagesMaxEMCItem();
        this.transTableManager.setPagesMaxEMCItem(cursorItem);
        this.updateExtractionRing();

        this.inventory.setItem(EMC_FILTER_SLOT, this.transTableManager.tagEMC2Item(cursorItem));
        this.player.setItemOnCursor(prevPagesMaxEMCItem);
    }

    private void filterItem2Cursor() {
        this.player.setItemOnCursor(this.transTableManager.getPagesMaxEMCItem());
        this.inventory.setItem(EMC_FILTER_SLOT, ItemStack.of(Material.AIR));

        this.page = 0;
        this.transTableManager.setPagesMaxEMCItem(ItemStack.of(Material.AIR));
        this.updateExtractionRing();
    }

    private void prevPage() {
        this.page = Math.max(this.page - 1, 0);
        this.updateExtractionRing();

        this.player.playSound(this.player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.5f, 1f);
        plugin.logger.info(this.page + " " + this.page);
    }

    private void nextPage() {
        this.page = Math.min(this.page + 1, this.transTableManager.getPagesSize() - 1);
        this.updateExtractionRing();

        this.player.playSound(this.player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.5f, 1f);
        plugin.logger.info(this.page + " " + this.page);

    }
}
