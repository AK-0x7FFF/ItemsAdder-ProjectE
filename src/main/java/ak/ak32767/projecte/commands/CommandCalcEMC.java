package ak.ak32767.projecte.commands;

import ak.ak32767.projecte.ProjectE;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public class CommandCalcEMC implements CommandExecutor {
    private final ProjectE plugin;

    public CommandCalcEMC(ProjectE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        try {
            this.plugin.getEmcBuilder().build(this.plugin.getWorldTransmutationBuilder());
        } catch (FileNotFoundException ignored) {}
        return true;
    }
}
