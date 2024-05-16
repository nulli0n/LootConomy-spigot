package su.nightexpress.lootconomy.command.children;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.command.CommandArguments;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BoosterCommand {

    private static final String ARG_NAME       = "name";
    private static final String ARG_CURRENCY   = "currency";
    private static final String ARG_MULTIPLIER = "multiplier";
    private static final String ARG_DURATION   = "duration";
    private static final String ARG_PLAYER     = "player";

    public static void inject(@NotNull LootConomyPlugin plugin, @NotNull ChainedNode node) {
        node.addChildren(ChainedNode.builder(plugin, "booster")
            .description(Lang.COMMAND_BOOSTER_DESC)
            .permission(Perms.COMMAND_BOOSTER)
            .addDirect("create", builder -> builder
                .description(Lang.COMMAND_BOOSTER_CREATE_DESC)
                .withArgument(ArgumentTypes.string(ARG_NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                )
                .withArgument(CommandArguments.currency(plugin, ARG_CURRENCY)
                    .required()
                )
                .withArgument(ArgumentTypes.decimalAbs(ARG_MULTIPLIER)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_MULTIPLIER)
                    .withSamples(tabContext -> Lists.newList("1.5", "2.0", "2.5", "3.0"))
                )
                .withArgument(ArgumentTypes.integerAbs(ARG_DURATION)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_DURATION)
                    .withSamples(tabContext -> Lists.newList("300", "3600", "7200", "86400"))
                )
                .withArgument(ArgumentTypes.playerName(ARG_PLAYER))
                .executes((context, arguments) -> executeCreate(plugin, context, arguments))
            )
            .addDirect("activate", builder -> builder
                .description(Lang.COMMAND_BOOSTER_ACTIVATE_DESC)
                .withArgument(ArgumentTypes.string(ARG_NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                    .withSamples(tabContext -> new ArrayList<>(plugin.getBoosterManager().getScheduledBoosterMap().keySet()))
                )
                .executes((context, arguments) -> executeActivate(plugin, context, arguments))
            )
            .addDirect("remove", builder -> builder
                .description(Lang.COMMAND_BOOSTER_REMOVE_DESC_GLOBAL)
                .withArgument(ArgumentTypes.string(ARG_NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                    .withSamples(tabContext -> new ArrayList<>(plugin.getBoosterManager().getBoosterMap().keySet()))
                )
                .executes((context, arguments) -> executeRemove(plugin, context, arguments))
            )
            .addDirect("removefor", builder -> builder
                .description(Lang.COMMAND_BOOSTER_REMOVE_DESC_PLAYER)
                .withArgument(ArgumentTypes.playerName(ARG_PLAYER)
                    .required())
                .withArgument(ArgumentTypes.string(ARG_NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                )
                .executes((context, arguments) -> executeRemoveFor(plugin, context, arguments))
            )
            .addDirect("info", builder -> builder
                .description(Lang.COMMAND_BOOSTER_INFO_DESC)
                .withArgument(ArgumentTypes.playerName(ARG_PLAYER))
                .executes((context, arguments) -> executeInfo(plugin, context, arguments))
            )
        );
    }

    public static boolean executeCreate(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(ARG_NAME);
        Currency currency = arguments.getArgument(ARG_CURRENCY, Currency.class);
        double modifier = arguments.getDoubleArgument(ARG_MULTIPLIER);
        int duration = arguments.getIntArgument(ARG_DURATION);

        Multiplier multiplier = new Multiplier().withCurrency(currency, modifier);
        ExpirableBooster booster = new ExpirableBooster(multiplier, duration);

        if (arguments.hasArgument(ARG_PLAYER)) {
            String playerName = arguments.getStringArgument(ARG_PLAYER);
            plugin.getUserManager().getUserDataAndPerformAsync(playerName, user -> {
                if (user == null) {
                    context.errorBadPlayer();
                    return;
                }

                user.addBooster(name, booster);

                context.send(Lang.COMMAND_BOOSTER_CREATE_DONE_PLAYER.getMessage()
                    .replace(Placeholders.GENERIC_NAME, name)
                    .replace(Placeholders.PLAYER_NAME, user.getName())
                    .replace(Placeholders.GENERIC_MULTIPLIER, NumberUtil.format(modifier))
                    .replace(Placeholders.GENERIC_DURATION, TimeUtil.formatDuration(booster.getExpireDate()))
                    .replace(currency.replacePlaceholders()));
            });
        }
        else {
            plugin.getBoosterManager().addBooster(name, booster, true);

            context.send(Lang.COMMAND_BOOSTER_CREATE_DONE_GLOBAL.getMessage()
                .replace(Placeholders.GENERIC_NAME, name)
                .replace(Placeholders.GENERIC_MULTIPLIER, NumberUtil.format(modifier))
                .replace(Placeholders.GENERIC_DURATION, TimeUtil.formatDuration(booster.getExpireDate()))
                .replace(currency.replacePlaceholders()));
        }

        return true;
    }

    public static boolean executeActivate(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(ARG_NAME);
        if (plugin.getBoosterManager().activateBooster(name)) {
            return context.sendSuccess(Lang.COMMAND_BOOSTER_ACTIVATE_DONE.getMessage());
        }
        return context.sendFailure(Lang.ERROR_INVALID_BOOSTER.getMessage());
    }

    public static boolean executeRemove(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(ARG_NAME);

        if (plugin.getBoosterManager().removeBooster(name)) {
            context.send(Lang.COMMAND_BOOSTER_REMOVE_DONE_GLOBAL.getMessage()
                .replace(Placeholders.GENERIC_NAME, name));
        }
        else {
            context.send(Lang.COMMAND_BOOSTER_REMOVE_ERROR_NOTHING.getMessage());
        }

        return true;
    }

    public static boolean executeRemoveFor(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(ARG_NAME);
        String playerName = arguments.getStringArgument(ARG_PLAYER);
        plugin.getUserManager().getUserDataAndPerformAsync(playerName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }

            if (user.removeBooster(name)) {
                context.send(Lang.COMMAND_BOOSTER_REMOVE_DONE_PLAYER.getMessage()
                    .replace(Placeholders.GENERIC_NAME, name)
                    .replace(Placeholders.PLAYER_NAME, user.getName()));
            }
            else {
                context.send(Lang.COMMAND_BOOSTER_REMOVE_ERROR_NOTHING.getMessage());
            }
        });
        return true;
    }

    public static boolean executeInfo(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Map<String, ExpirableBooster> boosterMap = new HashMap<>();

        if (arguments.hasArgument(ARG_PLAYER)) {
            String playerName = arguments.getStringArgument(ARG_PLAYER);
            AtomicBoolean check = new AtomicBoolean(true);
            plugin.getUserManager().getUserDataAndPerformAsync(playerName, user -> {
                if (user == null) {
                    context.errorBadPlayer();
                    check.set(false);
                    return;
                }

                boosterMap.putAll(user.getBoosterMap());
            });
            if (!check.get()) return false;
        }
        else {
            boosterMap.putAll(plugin.getBoosterManager().getBoosterMap());
        }

        if (boosterMap.isEmpty()) {
            return context.sendSuccess(Lang.COMMAND_BOOSTER_INFO_NOTHING.getMessage());
        }

        return context.sendSuccess(Lang.COMMAND_BOOSTER_INFO_LIST.getMessage()
            .replace(Placeholders.GENERIC_ENTRY, list -> {
                boosterMap.forEach((name, booster) -> {
                    String currencies = booster.getMultiplier().getCurrencyMap().entrySet().stream()
                        .map(entry -> Lang.COMMAND_BOOSTER_INFO_CURRENCY.getString()
                            .replace(Placeholders.CURRENCY_NAME, entry.getKey())
                            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(entry.getValue()))
                        ).collect(Collectors.joining(", "));

                    list.add(Lang.COMMAND_BOOSTER_INFO_ENTRY.getString()
                        .replace(Placeholders.GENERIC_NAME, name)
                        .replace(Placeholders.GENERIC_CURRENCY, currencies)
                    );
                });
            }));
    }
}
