package ak.ak32767.projecte;

import ak.ak32767.projecte.commands.CommandDebugEMC;
import ak.ak32767.projecte.commands.CommandDebugGUI;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.manager.EMCManager;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;
import ak.ak32767.projecte.listener.*;
import ak.ak32767.projecte.manager.KnowledgeManager;
import ak.ak32767.projecte.manager.TransmutationManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class ProjectE extends JavaPlugin {
    public final Logger logger = this.getLogger();
    private EMCManager emcManager;
    private TransmutationManager transmutationManager;
    private KnowledgeManager knowledgeManager;

    @Override
    public void onEnable() {
        File folder = this.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        PluginManager pluginManager = getServer().getPluginManager();

        this.transmutationManager = new TransmutationManager(this);

        // manager
        this.emcManager = new EMCManager(this);
        this.knowledgeManager = new KnowledgeManager(this);

        // listener register
        pluginManager.registerEvents(new EMCPreCalculateListener(this.transmutationManager), this);
        pluginManager.registerEvents(new GUIListener(this), this);
        pluginManager.registerEvents(new DataSave2PDCListener(this.emcManager, this.knowledgeManager), this);

        pluginManager.registerEvents(new IALoadedListener(this), this);
        pluginManager.registerEvents(new IAItemInteractListener(this), this);
        pluginManager.registerEvents(new IABlockPlaceListener(this), this);
        pluginManager.registerEvents(new IABlockBreakListener(this), this);
        pluginManager.registerEvents(new IABlockInteractListener(this), this);

        // command register
        getCommand("getemc").setExecutor(new CommandDebugEMC.GetEMC(this));
        getCommand("calcemc").setExecutor(new CommandDebugEMC.CalcEMC(this));
        getCommand("getplayeremc").setExecutor(new CommandDebugEMC.GetPlayerEMC(this));
        getCommand("setplayeremc").setExecutor(new CommandDebugEMC.SetPlayerEMC(this));
        getCommand("dgui").setExecutor(new CommandDebugGUI(this));
    }


    @Override
    public void onDisable() {
        this.emcManager.saveAllPlayerEMCMap2PDC();
        this.knowledgeManager.saveAllPlayerKnowledgeMap2PDC();
    }

    public EMCManager getEmcManager() {
        return this.emcManager;
    }

    public KnowledgeManager getKnowledgeManager() {
        return this.knowledgeManager;
    }

}
