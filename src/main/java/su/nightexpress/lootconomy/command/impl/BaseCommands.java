package su.nightexpress.lootconomy.command.impl;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.command.CommandArguments;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.money.MoneyUtils;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.impl.ReloadCommand;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;
import su.nightexpress.nightcore.util.CommandUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.random.Rnd;

public class BaseCommands {

    public static void load(@NotNull LootConomyPlugin plugin) {
        ChainedNode node = plugin.getRootNode();

        ReloadCommand.inject(plugin, node, Perms.COMMAND_RELOAD);

        node.addChildren(DirectNode.builder(plugin, "objectives")
            .description(Lang.COMMAND_OBJECTIVES_DESC)
            .permission(Perms.COMMAND_OBJECTIVES)
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(Perms.COMMAND_OBJECTIVES_OTHERS))
            .executes((context, arguments) -> openObjectives(plugin, context, arguments))
        );

        node.addChildren(DirectNode.builder(plugin, "sound")
            .description(Lang.COMMAND_SOUND_DESC)
            .permission(Perms.COMMAND_SOUND)
            .playerOnly()
            .executes((context, arguments) -> toggleSound(plugin, context, arguments))
        );

        node.addChildren(DirectNode.builder(plugin, "drop")
            .description(Lang.COMMAND_DROP_DESC)
            .permission(Perms.COMMAND_DROP)
            .withArgument(CommandArguments.currency(plugin, CommandArguments.CURRENCY)
                .required()
            )
            .withArgument(ArgumentTypes.decimalAbs(CommandArguments.MIN_AMOUNT)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_MIN)
                .withSamples(tabContext -> Lists.newList("10", "100"))
            )
            .withArgument(ArgumentTypes.decimalAbs(CommandArguments.MAX_AMOUNT)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_MAX)
                .withSamples(tabContext -> Lists.newList("20", "500"))
            )
            .withArgument(ArgumentTypes.integerAbs(CommandArguments.COUNT)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_COUNT)
                .withSamples(tabContext -> Lists.newList("1", "2", "3", "4", "5"))
            )
            .withArgument(ArgumentTypes.world(CommandArguments.WORLD).required())
            .withArgument(ArgumentTypes.decimal(CommandArguments.X)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_X)
                .withSamples(tabContext -> {
                    return tabContext.getPlayer() == null ? Lists.newList("0") : Lists.newList(NumberUtil.format(tabContext.getPlayer().getLocation().getBlockX()));
                })
            )
            .withArgument(ArgumentTypes.decimal(CommandArguments.Y)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_Y)
                .withSamples(tabContext -> {
                    return tabContext.getPlayer() == null ? Lists.newList("0") : Lists.newList(NumberUtil.format(tabContext.getPlayer().getLocation().getBlockY()));
                })
            )
            .withArgument(ArgumentTypes.decimal(CommandArguments.Z)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_Z)
                .withSamples(tabContext -> {
                    return tabContext.getPlayer() == null ? Lists.newList("0") : Lists.newList(NumberUtil.format(tabContext.getPlayer().getLocation().getBlockZ()));
                })
            )
            .executes((context, arguments) -> dropItem(plugin, context, arguments))
        );
    }

    public static boolean openObjectives(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        plugin.getMoneyManager().openObjectivesMenu(player);

        if (player != context.getSender()) {
            context.send(Lang.COMMAND_OBJECTIVES_DONE_OTHERS.getMessage().replace(Placeholders.forPlayer(player)));
        }

        return true;
    }

    public static boolean toggleSound(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        LootUser user = plugin.getUserManager().getUserData(player);

        user.getSettings().setPickupSound(!user.getSettings().isPickupSound());
        plugin.getUserManager().scheduleSave(user);

        return context.sendSuccess(Lang.COMMAND_SOUND_DONE.getMessage()
            .replace(Placeholders.GENERIC_STATE, Lang.getEnabledOrDisabled(user.getSettings().isPickupSound())));
    }

    public static boolean dropItem(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Currency currency = arguments.getArgument(CommandArguments.CURRENCY, Currency.class);
        World world = arguments.getWorldArgument(CommandArguments.WORLD);

        double min = arguments.getDoubleArgument(CommandArguments.MIN_AMOUNT);
        double max = arguments.getDoubleArgument(CommandArguments.MAX_AMOUNT);

        int count = Math.max(1, arguments.getIntArgument(CommandArguments.COUNT));

        double x = arguments.getDoubleArgument(CommandArguments.X);
        double y = arguments.getDoubleArgument(CommandArguments.Y);
        double z = arguments.getDoubleArgument(CommandArguments.Z);

        Location location = new Location(world, x, y, z);

        for (int i = 0; i < count; i++) {
            double amount = currency.round(Rnd.getDouble(min, max));
            if (amount <= 0) continue;

            ItemStack item = MoneyUtils.createMoney(currency, amount);
            world.dropItem(location, item);
        }

        return context.sendSuccess(Lang.COMMAND_DROP_DONE.getMessage()
            .replace(Placeholders.GENERIC_MIN, currency.format(min))
            .replace(Placeholders.GENERIC_MAX, currency.format(max))
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(count))
            .replace(Placeholders.forLocation(location)));
    }
}
