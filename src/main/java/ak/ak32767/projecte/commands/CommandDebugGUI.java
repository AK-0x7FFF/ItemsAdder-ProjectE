package ak.ak32767.projecte.commands;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.gui.TransTableGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandDebugGUI implements CommandExecutor {
    private final ProjectE plugin;

    public CommandDebugGUI(ProjectE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Player player = null;
        if (sender instanceof Player)
            player = ((Player) sender).getPlayer();;

        if (player == null) {
            return false;
        }

        new TransTableGUI(this.plugin, player);
        return true;
    }
}