package su.nightexpress.lootconomy.command.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.command.CommandArguments;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BoosterCommands {

    public static void load(@NotNull LootConomyPlugin plugin) {
        ChainedNode node = plugin.getRootNode();

        node.addChildren(DirectNode.builder(plugin, "boosts")
            .description(Lang.COMMAND_BOOSTS_DESC)
            .permission(Perms.COMMAND_BOOSTS)
            .executes((context, arguments) -> showBoosts(plugin, context))
        );

        node.addChildren(ChainedNode.builder(plugin, "booster")
            .description(Lang.COMMAND_BOOSTER_DESC)
            .permission(Perms.COMMAND_BOOSTER)
            .addDirect("create", builder -> builder
                .description(Lang.COMMAND_BOOSTER_CREATE_DESC)
                .withArgument(ArgumentTypes.string(CommandArguments.NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                )
                .withArgument(CommandArguments.currency(plugin, CommandArguments.CURRENCY).required())
                .withArgument(ArgumentTypes.decimalAbs(CommandArguments.MULTIPLIER)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_MULTIPLIER)
                    .withSamples(tabContext -> Lists.newList("1.5", "2.0", "2.5", "3.0"))
                )
                .withArgument(ArgumentTypes.integerAbs(CommandArguments.DURATION)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_DURATION)
                    .withSamples(tabContext -> Lists.newList("300", "3600", "7200", "86400"))
                )
                .withArgument(ArgumentTypes.playerName(CommandArguments.PLAYER))
                .executes((context, arguments) -> create(plugin, context, arguments))
            )
            .addDirect("activate", builder -> builder
                .description(Lang.COMMAND_BOOSTER_ACTIVATE_DESC)
                .withArgument(ArgumentTypes.string(CommandArguments.NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                    .withSamples(tabContext -> new ArrayList<>(plugin.getBoosterManager().getScheduledBoosterMap().keySet()))
                )
                .executes((context, arguments) -> activate(plugin, context, arguments))
            )
            .addDirect("remove", builder -> builder
                .description(Lang.COMMAND_BOOSTER_REMOVE_DESC_GLOBAL)
                .withArgument(ArgumentTypes.string(CommandArguments.NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                    .withSamples(tabContext -> new ArrayList<>(plugin.getBoosterManager().getBoosterMap().keySet()))
                )
                .executes((context, arguments) -> remove(plugin, context, arguments))
            )
            .addDirect("removefor", builder -> builder
                .description(Lang.COMMAND_BOOSTER_REMOVE_DESC_PLAYER)
                .withArgument(ArgumentTypes.playerName(CommandArguments.PLAYER)
                    .required())
                .withArgument(ArgumentTypes.string(CommandArguments.NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                )
                .executes((context, arguments) -> removeFor(plugin, context, arguments))
            )
            .addDirect("info", builder -> builder
                .description(Lang.COMMAND_BOOSTER_INFO_DESC)
                .withArgument(ArgumentTypes.playerName(CommandArguments.PLAYER))
                .executes((context, arguments) -> printInfo(plugin, context, arguments))
            )
        );
    }

    private static boolean showBoosts(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context) {
        plugin.getBoosterManager().printBoosters(context.getSender());
        return true;
    }

    private static boolean create(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(CommandArguments.NAME);
        Currency currency = arguments.getArgument(CommandArguments.CURRENCY, Currency.class);
        double modifier = arguments.getDoubleArgument(CommandArguments.MULTIPLIER);
        int duration = arguments.getIntArgument(CommandArguments.DURATION);

        Multiplier multiplier = new Multiplier().withCurrency(currency, modifier);
        ExpirableBooster booster = new ExpirableBooster(multiplier, duration);

        if (arguments.hasArgument(CommandArguments.PLAYER)) {
            String playerName = arguments.getStringArgument(CommandArguments.PLAYER);
            plugin.getUserManager().manageUser(playerName, user -> {
                if (user == null) {
                    context.errorBadPlayer();
                    return;
                }

                user.addBooster(name, booster);
                plugin.getUserManager().save(user);

                Lang.COMMAND_BOOSTER_CREATE_DONE_PLAYER.getMessage().send(context.getSender(), replacer -> replacer
                    .replace(Placeholders.GENERIC_NAME, name)
                    .replace(Placeholders.PLAYER_NAME, user.getName())
                    .replace(Placeholders.GENERIC_MULTIPLIER, NumberUtil.format(modifier))
                    .replace(Placeholders.GENERIC_DURATION, TimeUtil.formatDuration(booster.getExpireDate()))
                    .replace(currency.replacePlaceholders())
                );
            });
        }
        else {
            plugin.getBoosterManager().addBooster(name, booster, true);

            Lang.COMMAND_BOOSTER_CREATE_DONE_GLOBAL.getMessage().send(context.getSender(), replacer -> replacer
                .replace(Placeholders.GENERIC_NAME, name)
                .replace(Placeholders.GENERIC_MULTIPLIER, NumberUtil.format(modifier))
                .replace(Placeholders.GENERIC_DURATION, TimeUtil.formatDuration(booster.getExpireDate()))
                .replace(currency.replacePlaceholders())
            );
        }

        return true;
    }

    private static boolean activate(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(CommandArguments.NAME);
        if (plugin.getBoosterManager().activateBooster(name)) {
            return context.sendSuccess(Lang.COMMAND_BOOSTER_ACTIVATE_DONE.getMessage());
        }
        return context.sendFailure(Lang.ERROR_INVALID_BOOSTER.getMessage());
    }

    private static boolean remove(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(CommandArguments.NAME);

        if (plugin.getBoosterManager().removeBooster(name)) {
            Lang.COMMAND_BOOSTER_REMOVE_DONE_GLOBAL.getMessage().send(context.getSender(), replacer -> replacer
                .replace(Placeholders.GENERIC_NAME, name)
            );
        }
        else {
            context.send(Lang.COMMAND_BOOSTER_REMOVE_ERROR_NOTHING.getMessage());
        }

        return true;
    }

    private static boolean removeFor(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(CommandArguments.NAME);
        String playerName = arguments.getStringArgument(CommandArguments.PLAYER);
        plugin.getUserManager().manageUser(playerName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }

            if (user.removeBooster(name)) {
                plugin.getUserManager().save(user);
                Lang.COMMAND_BOOSTER_REMOVE_DONE_PLAYER.getMessage().send(context.getSender(), replacer -> replacer
                    .replace(Placeholders.GENERIC_NAME, name)
                    .replace(Placeholders.PLAYER_NAME, user.getName())
                );
            }
            else {
                context.send(Lang.COMMAND_BOOSTER_REMOVE_ERROR_NOTHING.getMessage());
            }
        });
        return true;
    }

    private static boolean printInfo(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Map<String, ExpirableBooster> boosterMap = new HashMap<>();

        if (arguments.hasArgument(CommandArguments.PLAYER)) {
            String playerName = arguments.getStringArgument(CommandArguments.PLAYER);
            AtomicBoolean check = new AtomicBoolean(true);
            plugin.getUserManager().manageUser(playerName, user -> {
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

        Lang.COMMAND_BOOSTER_INFO_LIST.getMessage().send(context.getSender(), replacer -> replacer
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
            })
        );

        return true;
    }
}
