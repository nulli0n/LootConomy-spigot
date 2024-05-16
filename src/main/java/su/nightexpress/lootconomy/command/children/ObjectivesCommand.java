package su.nightexpress.lootconomy.command.children;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;
import su.nightexpress.nightcore.util.CommandUtil;

public class ObjectivesCommand {

    private static final String ARG_PLAYER = "player";

    public static void inject(@NotNull LootConomyPlugin plugin, @NotNull ChainedNode node) {
        node.addChildren(DirectNode.builder(plugin, "objectives")
            .description(Lang.COMMAND_OBJECTIVES_DESC)
            .permission(Perms.COMMAND_OBJECTIVES)
            .withArgument(ArgumentTypes.player(ARG_PLAYER).permission(Perms.COMMAND_OBJECTIVES_OTHERS))
            .executes((context, arguments) -> execute(plugin, context, arguments))
        );
    }

    public static boolean execute(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, ARG_PLAYER);
        if (player == null) return false;

        plugin.getMoneyManager().openObjectivesMenu(player);

        if (player != context.getSender()) {
            context.send(Lang.COMMAND_OBJECTIVES_DONE_OTHERS.getMessage().replace(Placeholders.forPlayer(player)));
        }

        return true;
    }
}
