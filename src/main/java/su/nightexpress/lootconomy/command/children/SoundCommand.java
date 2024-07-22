package su.nightexpress.lootconomy.command.children;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;

public class SoundCommand {

    public static void inject(@NotNull LootConomyPlugin plugin, @NotNull ChainedNode node) {
        node.addChildren(DirectNode.builder(plugin, "sound")
            .description(Lang.COMMAND_SOUND_DESC)
            .permission(Perms.COMMAND_SOUND)
            .playerOnly()
            .executes((context, arguments) -> execte(plugin, context, arguments))
        );
    }

    public static boolean execte(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        LootUser user = plugin.getUserManager().getUserData(player);

        user.getSettings().setPickupSound(!user.getSettings().isPickupSound());
        plugin.getUserManager().saveAsync(user);

        return context.sendSuccess(Lang.COMMAND_SOUND_DONE.getMessage()
            .replace(Placeholders.GENERIC_STATE, Lang.getEnabledOrDisabled(user.getSettings().isPickupSound())));
    }
}
