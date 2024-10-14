package su.nightexpress.lootconomy.command;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.nightcore.command.experimental.argument.CommandArgument;
import su.nightexpress.nightcore.command.experimental.builder.ArgumentBuilder;

public class CommandArguments {

    public static final String PLAYER     = "player";
    public static final String CURRENCY   = "currency";
    public static final String MIN_AMOUNT = "min";
    public static final String MAX_AMOUNT = "max";
    public static final String COUNT      = "count";
    public static final String WORLD      = "world";
    public static final String X          = "x";
    public static final String Y          = "y";
    public static final String Z          = "z";
    public static final String NAME       = "name";
    public static final String MULTIPLIER = "multiplier";
    public static final String DURATION   = "duration";

    @NotNull
    public static ArgumentBuilder<Currency> currency(@NotNull LootConomyPlugin plugin, @NotNull String name) {
        return CommandArgument.builder(name, (str, context) -> plugin.getCurrencyManager().getCurrency(str))
            .localized(Lang.COMMAND_ARGUMENT_NAME_CURRENCY)
            .customFailure(Lang.ERROR_INVALID_CURRENCY)
            .withSamples(tabContext -> plugin.getCurrencyManager().getCurrencyIds())
            ;
    }
}
