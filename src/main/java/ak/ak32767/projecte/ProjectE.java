package ak.ak32767.projecte;

import ak.ak32767.projecte.commands.CommandCalcEMC;
import ak.ak32767.projecte.commands.CommandGetEMC;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.emcsys.EMCManager;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;
import ak.ak32767.projecte.listener.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class ProjectE extends JavaPlugin {
    private WorldTransmutationsBuilder worldTransmutationBuilder;
    private EMCBuilder emcBuilder;
    private EMCManager emcManager;
    public final Logger logger = this.getLogger();

    @Override
    public void onEnable() {
        PluginManager pluginManager = getServer().getPluginManager();

        this.worldTransmutationBuilder = new WorldTransmutationsBuilder();
        this.worldTransmutationBuilder.build();

        // 在 `IALoadedListener.java` 中進行BUILD
        this.emcBuilder = new EMCBuilder(this);
        this.emcManager = new EMCManager(this);

        // listener register
        pluginManager.registerEvents(new EMCSave2PDCListener(this.emcManager), this);

        pluginManager.registerEvents(new IALoadedListener(this), this);
        pluginManager.registerEvents(new IABlockPlaceListener(this), this);
        pluginManager.registerEvents(new IABlockBreakListener(this), this);
        pluginManager.registerEvents(new IABlockInteractListener(this), this);

        // command register
        getCommand("getemc").setExecutor(new CommandGetEMC(this));
        getCommand("calcemc").setExecutor(new CommandCalcEMC(this));
    }


    @Override
    public void onDisable() {
        this.emcManager.saveAllPlayerEMCMap2PDC();
    }

    public EMCBuilder getEmcBuilder() {
        return this.emcBuilder;
    }

    public EMCManager getEmcManager() {
        return emcManager;
    }

    public WorldTransmutationsBuilder getWorldTransmutationBuilder() {
        return worldTransmutationBuilder;
    }
}
