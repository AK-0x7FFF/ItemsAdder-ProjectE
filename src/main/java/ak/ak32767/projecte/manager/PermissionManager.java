package ak.ak32767.projecte.manager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import java.util.UUID;

public enum PermissionManager {
    ADMIN ("projecte.admin"),
    RELOAD("projecte.reload"),

    TRANSTABLE_LEARN    ("projecte.transtable.learn"),
    TRANSTABLE_UNLEARN  ("projecte.transtable.unlearn"),
    TRANSTABLE_TRANSMUTE("projecte.transtable.transmute"),
    TRANSTABLE_EXTRACT  ("projecte.transtable.extract"),

    PHILOSTONE_TRANSITEM ("projecte.philostone.transitem"),
    PHILOSTONE_TRANSBLOCK("projecte.philostone.transblock");

    private final String permID;

    PermissionManager(String permID) {
        this.permID = permID;
    }

    public String getPermID() {
        return this.permID;
    }

    public boolean check(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return this.check(player);
    }

    public boolean check(CommandSender sender) {
        if (sender == null)
            return false;

        if (sender instanceof ConsoleCommandSender)
            return true;

        return sender.hasPermission(this.permID);
    }

    public static void register(PluginManager pm) {
        for (var perm : PermissionManager.values()) {
            if (pm.getPermission(perm.getPermID()) != null)
                continue;

            Permission bukkitPerm = new Permission(perm.getPermID());
            pm.addPermission(bukkitPerm);
        }

    }
}
