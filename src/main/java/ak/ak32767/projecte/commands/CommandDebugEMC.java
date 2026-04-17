package ak.ak32767.projecte.commands;

import ak.ak32767.projecte.ProjectE;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class CommandDebugEMC {
    public CommandDebugEMC() {
    }

    public static class CalcEMC implements CommandExecutor {
        private final ProjectE plugin;

        public CalcEMC(ProjectE plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
            this.plugin.getEmcManager().build();
            return true;
        }
    }


    public static class GetEMC implements CommandExecutor {
        private final ProjectE plugin;

        public GetEMC(ProjectE plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            ItemStack item = null;
            String name = "NULL";

            if (args.length == 0) {
                if (sender instanceof Player player) {
                    item = player.getInventory().getItemInMainHand();
                    name = PlainTextComponentSerializer.plainText().serialize(item.displayName());
                } else
                    return false;
            } else {
                Material material = Material.getMaterial(args[0].toUpperCase());
                if (material == null)
                    return false;

                item = ItemStack.of(material);
                name = args[0].toUpperCase();
            }

            sender.sendMessage("Material [" + name + "] EMC: " + this.plugin.getEmcManager().getItemEMC(item).toString());
            var log = this.plugin.getEmcManager().getItemEMCCalcLog(item);
            if (log == null)
                return true;

            for (String s : log)
                sender.sendMessage("    " + s);
            return true;
        }
    }


    public static class GetPlayerEMC implements CommandExecutor {
        private final ProjectE plugin;

        public GetPlayerEMC(ProjectE plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            Player player = null;
            if (sender instanceof Player)
                player = ((Player) sender).getPlayer();;

            if (player == null) {
                return false;
            }

            sender.sendMessage("Player Has [" + this.plugin.getEmcManager().getPlayerEMC(player) + "] EMCs");
            return true;
        }
    }


    public static class SetPlayerEMC implements CommandExecutor {
        private final ProjectE plugin;

        public SetPlayerEMC(ProjectE plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            Player player = null;
            if (sender instanceof Player)
                player = ((Player) sender).getPlayer();

            if (player == null) {
                return false;
            }

            if (args.length == 0)
                return false;

            BigInteger prev = this.plugin.getEmcManager().getPlayerEMC(player);
            BigInteger value = new BigInteger(args[0]);
            this.plugin.getEmcManager().setPlayerEMC(player, value);

            sender.sendMessage("Set Player EMC: " + prev + " -> " + value);
            return true;
        }
    }
}
