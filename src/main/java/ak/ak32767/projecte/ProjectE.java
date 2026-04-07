package ak.ak32767.projecte;

import ak.ak32767.projecte.commands.CommandGetEMC;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;
import ak.ak32767.projecte.listener.IABlockBreakListener;
import ak.ak32767.projecte.listener.IABlockInteractListener;
import ak.ak32767.projecte.listener.IABlockPlaceListener;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ProjectE extends JavaPlugin {
    private WorldTransmutationsBuilder worldTransmutationBuilder;
    private EMCBuilder emcBuilder;
    public final Logger logger = this.getLogger();

    @Override
    public void onEnable() {
//        this.logger.setLevel(Level.ALL);

        this.worldTransmutationBuilder = new WorldTransmutationsBuilder();
        this.worldTransmutationBuilder.build();

        this.emcBuilder = new EMCBuilder(this);
        this.emcBuilder.build(this.worldTransmutationBuilder);

        // listener register
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new IABlockPlaceListener(this), this);
        pluginManager.registerEvents(new IABlockBreakListener(this), this);
        pluginManager.registerEvents(new IABlockInteractListener(this), this);

        // command register
        getCommand("getemc").setExecutor(new CommandGetEMC(this));

//        // protocollib rejister
//        var protocolManager = ProtocolLibrary.getProtocolManager();
//        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS) {
//            @Override
//            public void onPacketSending(PacketEvent event) {
//                PacketContainer packet = event.getPacket();
//
//                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
//                    ItemStack item = packet.getItemModifier().read(0);
//
//                    if (item != null && item.getType() != Material.AIR) {
//                        packet.getItemModifier().write(0, ((ProjectE) this.plugin).inject(item.clone()));
//                    }
//                } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
//                    List<ItemStack> items = packet.getItemListModifier().read(0);
//                    List<ItemStack> newItems = new ArrayList<>();
//
//                    for (ItemStack item : items) {
//                        if (item != null && item.getType() != Material.AIR) {
//                            newItems.add(((ProjectE) this.plugin).inject(item.clone()));
//                        } else {
//                            newItems.add(item);
//                        }
////                        newItems.add(((ProjectE) this.plugin).inject(item.clone()));
//                    }
//                    // 寫回封包
//                    packet.getItemListModifier().write(0, newItems);
//                }
//            }
//        });

    }

//    private ItemStack inject(ItemStack clone) {
//        ItemMeta meta = clone.getItemMeta();
//        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
//        var emc = this.emcBuilder.getItemEmc(clone.getType());
//
//        lore.removeIf(line -> line.contains(Component.text("EMC: ")));
//        lore.add(Component.text("EMC: ").append(Component.text(emc.toString())));
//
//        meta.lore(lore);
//        clone.setItemMeta(meta);
//
//        return clone;
//    }

    @Override
    public void onDisable() {
    }

    public EMCBuilder getEmcBuilder() {
        return this.emcBuilder;
    }

    public WorldTransmutationsBuilder getWorldTransmutationBuilder() {
        return worldTransmutationBuilder;
    }
}
