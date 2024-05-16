package su.nightexpress.lootconomy.command.children;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;

public class BoostsCommand {

    public static void inject(@NotNull LootConomyPlugin plugin, @NotNull ChainedNode node) {
        node.addChildren(DirectNode.builder(plugin, "boosts")
            .description(Lang.COMMAND_BOOSTS_DESC)
            .permission(Perms.COMMAND_BOOSTS)
            .executes((context, arguments) -> execute(plugin, context, arguments))
        );
    }

    public static boolean execute(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        plugin.getBoosterManager().printBoosters(context.getSender());
        return true;
    }
}
