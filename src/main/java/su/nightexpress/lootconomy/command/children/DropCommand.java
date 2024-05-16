package su.nightexpress.lootconomy.command.children;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.command.CommandArguments;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.money.MoneyUtils;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.random.Rnd;

public class DropCommand {

    private static final String ARG_CURRENCY   = "currency";
    private static final String ARG_MIN_AMOUNT = "min";
    private static final String ARG_MAX_AMOUNT = "max";
    private static final String ARG_COUNT      = "count";
    private static final String ARG_WORLD      = "world";
    private static final String ARG_X          = "x";
    private static final String ARG_Y          = "y";
    private static final String ARG_Z          = "z";

    public static void inject(@NotNull LootConomyPlugin plugin, @NotNull ChainedNode node) {
        node.addChildren(DirectNode.builder(plugin, "drop")
            .description(Lang.COMMAND_DROP_DESC)
            .permission(Perms.COMMAND_DROP)
            .withArgument(CommandArguments.currency(plugin, ARG_CURRENCY)
                .required()
            )
            .withArgument(ArgumentTypes.decimalAbs(ARG_MIN_AMOUNT)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_MIN)
                .withSamples(tabContext -> Lists.newList("10", "100"))
            )
            .withArgument(ArgumentTypes.decimalAbs(ARG_MAX_AMOUNT)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_MAX)
                .withSamples(tabContext -> Lists.newList("20", "500"))
            )
            .withArgument(ArgumentTypes.integerAbs(ARG_COUNT)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_COUNT)
                .withSamples(tabContext -> Lists.newList("1", "2", "3", "4", "5"))
            )
            .withArgument(ArgumentTypes.world(ARG_WORLD).required())
            .withArgument(ArgumentTypes.decimal(ARG_X)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_X)
                .withSamples(tabContext -> {
                    return tabContext.getPlayer() == null ? Lists.newList("0") : Lists.newList(NumberUtil.format(tabContext.getPlayer().getLocation().getBlockX()));
                })
            )
            .withArgument(ArgumentTypes.decimal(ARG_Y)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_Y)
                .withSamples(tabContext -> {
                    return tabContext.getPlayer() == null ? Lists.newList("0") : Lists.newList(NumberUtil.format(tabContext.getPlayer().getLocation().getBlockY()));
                })
            )
            .withArgument(ArgumentTypes.decimal(ARG_Z)
                .required()
                .localized(Lang.COMMAND_ARGUMENT_NAME_Z)
                .withSamples(tabContext -> {
                    return tabContext.getPlayer() == null ? Lists.newList("0") : Lists.newList(NumberUtil.format(tabContext.getPlayer().getLocation().getBlockZ()));
                })
            )
            .executes((context, arguments) -> execute(plugin, context, arguments))
        );
    }



    public static boolean execute(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Currency currency = arguments.getArgument(ARG_CURRENCY, Currency.class);
        World world = arguments.getWorldArgument(ARG_WORLD);

        double min = arguments.getDoubleArgument(ARG_MIN_AMOUNT);
        double max = arguments.getDoubleArgument(ARG_MAX_AMOUNT);


        int count = Math.max(1, arguments.getIntArgument(ARG_COUNT));

        double x = arguments.getDoubleArgument(ARG_X);
        double y = arguments.getDoubleArgument(ARG_Y);
        double z = arguments.getDoubleArgument(ARG_Z);

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
