package su.nightexpress.lootconomy.command;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.nightcore.command.experimental.argument.CommandArgument;
import su.nightexpress.nightcore.command.experimental.builder.ArgumentBuilder;

public class CommandArguments {

    @NotNull
    public static ArgumentBuilder<Currency> currency(@NotNull LootConomyPlugin plugin, @NotNull String name) {
        return CommandArgument.builder(name, str -> plugin.getCurrencyManager().getCurrency(str))
            .localized(Lang.COMMAND_ARGUMENT_NAME_CURRENCY)
            .customFailure(Lang.ERROR_INVALID_CURRENCY)
            .withSamples(tabContext -> plugin.getCurrencyManager().getCurrencyIds())
            ;
    }
}
