package ak.ak32767.projecte.commands;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.data.ExactItemWrapper;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CommandGetEMC implements CommandExecutor {
    private final ProjectE plugin;

    public CommandGetEMC(ProjectE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Object target = null;
        String name = "NULL";

        if (args.length == 0) {
            if (sender instanceof Player player) {
                ItemStack item = player.getInventory().getItemInMainHand();
                target = ExactItemWrapper.of(item);
//                plugin.logger.info(target.getClass().toString());
                name = PlainTextComponentSerializer.plainText().serialize(item.displayName());
            } else
                return false;
        } else {
            target = Material.getMaterial(args[0].toUpperCase());
            name = args[0].toUpperCase();
        }

        if (target == null) {
            return false;
        }

        sender.sendMessage("Material [" + name + "] EMC: " + this.plugin.getEmcBuilder().getEmc(target).toString());
        return true;
    }
}
