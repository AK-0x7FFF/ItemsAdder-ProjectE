package ak.ak32767.projecte;

import ak.ak32767.projecte.commands.CommandDebugEMC;
import ak.ak32767.projecte.commands.CommandDebugGUI;
import ak.ak32767.projecte.commands.CommandProjectE;
import ak.ak32767.projecte.listener.*;
import ak.ak32767.projecte.listener.itemsadder.IABlockBreakListener;
import ak.ak32767.projecte.listener.itemsadder.IABlockInteractListener;
import ak.ak32767.projecte.listener.itemsadder.IABlockPlaceListener;
import ak.ak32767.projecte.listener.itemsadder.IAItemInteractListener;
import ak.ak32767.projecte.manager.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class ProjectE extends JavaPlugin {
    public final Logger logger = this.getLogger();

    private ConfigManager configManager;
    private TransmutationManager transmutationManager;
    private EMCManager emcManager;
    private KnowledgeManager knowledgeManager;

    @Override
    public void onEnable() {
        PluginManager pluginManager = getServer().getPluginManager();
        PermissionManager.register(pluginManager);
        this.configManager = new ConfigManager(this);

        this.transmutationManager = new TransmutationManager(this);

        // manager
        this.emcManager = new EMCManager(this);
        this.knowledgeManager = new KnowledgeManager(this);

        // listener register
        pluginManager.registerEvents(new EMCPreCalculateListener(this.transmutationManager), this);
        pluginManager.registerEvents(new PhiloRecipeListener(this.transmutationManager.getPhiloCraftTransmutation(), this), this);
        pluginManager.registerEvents(new GUIListener(this), this);
        pluginManager.registerEvents(new DataSave2PDCListener(this.emcManager, this.knowledgeManager), this);

        pluginManager.registerEvents(new IALoadedListener(this), this);
        pluginManager.registerEvents(new IAItemInteractListener(this), this);
        pluginManager.registerEvents(new IABlockPlaceListener(this), this);
        pluginManager.registerEvents(new IABlockBreakListener(this), this);
        pluginManager.registerEvents(new IABlockInteractListener(this), this);

        // command register
        PluginCommand peCommand = getCommand("projecte");
        if (peCommand != null) {
            peCommand.setExecutor(new CommandProjectE.Executor(this));
            peCommand.setTabCompleter(new CommandProjectE.Completer(this));
        }

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

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public EMCManager getEmcManager() {
        return this.emcManager;
    }

    public KnowledgeManager getKnowledgeManager() {
        return this.knowledgeManager;
    }

}
