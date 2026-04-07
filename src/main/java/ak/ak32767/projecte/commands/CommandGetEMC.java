package ak.ak32767.projecte.commands;

import ak.ak32767.projecte.ProjectE;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class CommandGetEMC implements CommandExecutor {
    private final ProjectE plugin;

    public CommandGetEMC(ProjectE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Material material = null;

        if (args.length == 0) {
            if (sender instanceof Player player)
                material = player.getInventory().getItemInMainHand().getType();
            else
                return false;
        } else
            material = Material.getMaterial(args[0].toUpperCase());

        if (material == null) {
            return false;
        }

        String materialName = material.name();
        sender.sendMessage("Material [" + materialName + "] EMC: " + this.plugin.getEmcBuilder().getItemEmc(material).toString());
        return true;
    }
}
