package ak.ak32767.projecte.commands;

import ak.ak32767.projecte.ProjectE;
import dev.lone.itemsadder.api.CustomStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandProjectE {
    public static class Executor implements CommandExecutor {
        private final ProjectE plugin;

        public Executor(ProjectE plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
            if (args.length == 0) {
                sender.sendMessage("meow");
                return true;
            }

            switch (args[0].toLowerCase()) {
                // projecte emc
                case "emc" -> {
                    switch (args[1].toLowerCase()) {
                        // projecte emc get <player>
                        case "get" -> {
                            if (args.length < 3) {
                                break;
                            }

                            Player player = Bukkit.getPlayer(args[2]);
                            if (player == null) {
                                break;
                            }

                            BigInteger emc = this.plugin.getEmcManager().getPlayerEMC(player);
                            sender.sendMessage(player.getName() + "has EMC: " + emc);
                        }
                        // projecte emc set <player> <value>
                        case "set" -> {
                            if (args.length < 4) {
                                break;
                            }

                            Player player = Bukkit.getPlayer(args[2]);
                            if (player == null) {
                                break;
                            }

                            BigInteger emc;
                            try { emc = new BigInteger(args[3]); }
                            catch (NumberFormatException e) {
                                break;
                            }

                            this.plugin.getEmcManager().setPlayerEMC(player, emc);
                        }
                        // projecte emc add <player> <value>
                        case "add" -> {
                            if (args.length < 4) {
                                break;
                            }

                            Player player = Bukkit.getPlayer(args[2]);
                            if (player == null) {
                                break;
                            }

                            BigInteger emc;
                            try { emc = new BigInteger(args[3]); }
                            catch (NumberFormatException e) {
                                break;
                            }

                            this.plugin.getEmcManager().addPlayerEMC(player, emc);
                        }
                        // projecte emc reload
                        case "reload" -> {
                            this.plugin.getEmcManager().build();
                        }
                        default -> {}
                    }
                }
                case "knowledge" -> {
                    if (args.length < 4) {
                        break;
                    }

                    Player player = Bukkit.getPlayer(args[2]);
                    if (player == null) {
                        break;
                    }

                    ItemStack item = this.plugin.getKnowledgeManager().knowledgeFormat2Item(args[3]);
                    if (item == null) {
                        break;
                    }

                    if (!this.plugin.getKnowledgeManager().isLearnable(item)) {
                        break;
                    }

                    switch (args[1].toLowerCase()) {
                        case "learn" -> {
                            this.plugin.getKnowledgeManager().learn(player, item);
                        }
                        case "unlearn" -> {
                            if (this.plugin.getKnowledgeManager().isLearned(player, item))
                                this.plugin.getKnowledgeManager().unlearn(player, item);
                        }
                        default -> {}
                    }
                }
                default -> {}
            }

            return true;
        }
    }

    public static class Completer implements TabCompleter {
        private static final List<String> EMPTY = Collections.emptyList();
        private static final List<String> ROOT_SUBS = List.of("emc", "knowledge");
        private static final List<String> EMC_SUBS = List.of("get", "set", "add", "reload");
        private static final List<String> EMC_SET_OR_ADD_SUBS = List.of("<Number>");
        private static final List<String> KNOWLEDGE_SUBS = List.of("learn", "unlearn");

        private static final List<String> BASE_NAMESPACE = List.of(NamespacedKey.MINECRAFT_NAMESPACE + ":");

        private final ProjectE plugin;

        public Completer(ProjectE plugin) {
            this.plugin = plugin;
        }

        private static List<String> filter(List<String> list, String input) {
            List<String> result = new ObjectArrayList<>();
            StringUtil.copyPartialMatches(input, list, result);
            return result;
        }

        private static List<String> getPlayers() {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toCollection(ObjectArrayList::new));
        }

        private static List<String> getNamespaces() {
            List<String> namespaces = CustomStack.getNamespacedIdsInRegistry().stream()
                .map(namespacedID -> {
                    NamespacedKey key = NamespacedKey.fromString(namespacedID);
                    return (key != null) ? key.getNamespace() + ":" : "itemsadder:";
                })
                .distinct()
                .collect(Collectors.toCollection(ObjectArrayList::new));
            namespaces.addAll(BASE_NAMESPACE);
            return namespaces;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
            List<String> commands = EMPTY;

            if (args.length == 1) {
                commands = filter(ROOT_SUBS, args[0]);

            } else if (args.length > 1) {
                commands = switch (args[0].toLowerCase()) {
                    default -> EMPTY;

                    case "emc" -> switch (args.length) {
                        default -> EMPTY;
                        case 2 -> filter(EMC_SUBS, args[1]);

                        case 3 -> switch (args[1].toLowerCase()) {
                            case "get", "set", "add" -> filter(getPlayers(), args[2]);
                            default -> EMPTY;
                        };

                        case 4 -> switch (args[1].toLowerCase()) {
                            case "set", "add" -> EMC_SET_OR_ADD_SUBS;
                            default -> EMPTY;
                        };
                    };

                    case "knowledge" -> switch (args.length) {
                        default -> EMPTY;
                        case 2 -> filter(KNOWLEDGE_SUBS, args[1]);

                        case 3 -> switch (args[1].toLowerCase()) {
                            case "learn", "unlearn" -> filter(getPlayers(), args[2]);
                            default -> EMPTY;
                        };

                        case 4 -> switch (args[1].toLowerCase()) {
                            default -> EMPTY;

                            case "learn" -> {
                                if (args[3].contains(":")) {
                                    yield filter(this.plugin.getKnowledgeManager().getAllLearnableItem(), args[3]).stream()
                                        .limit(20)
                                        .collect(Collectors.toCollection(ObjectArrayList::new));
                                }
                                yield getNamespaces();
                            }
                            case "unlearn" -> {
                                Player player = Bukkit.getPlayer(args[2]);
                                if  (player == null)
                                    yield EMPTY;

                                if (args[3].contains(":")) {
                                    yield filter(
                                        this.plugin.getKnowledgeManager().getPlayerKnowledge(player).stream()
                                        .collect(Collectors.toCollection(ObjectArrayList::new)), args[3]
                                    ).stream()
                                        .limit(20)
                                        .collect(Collectors.toCollection(ObjectArrayList::new));
                                }
                                yield getNamespaces();
                            }

                        };
                    };
                };
            }

            return commands;
        }
    }
}
