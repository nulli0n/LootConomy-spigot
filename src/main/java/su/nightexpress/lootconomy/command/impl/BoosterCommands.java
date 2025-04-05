package su.nightexpress.lootconomy.command.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.command.CommandArguments;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.util.ArrayList;

public class BoosterCommands {

    public static void load(@NotNull LootConomyPlugin plugin) {
        ChainedNode node = plugin.getRootNode();

        node.addChildren(DirectNode.builder(plugin, "boosts")
            .playerOnly()
            .description(Lang.COMMAND_BOOSTS_DESC)
            .permission(Perms.COMMAND_BOOSTS)
            .executes((context, arguments) -> showBoosts(plugin, context))
        );

        node.addChildren(ChainedNode.builder(plugin, "booster")
            .description(Lang.COMMAND_BOOSTER_DESC)
            .permission(Perms.COMMAND_BOOSTER)
            .addDirect("create", builder -> builder
                .description(Lang.COMMAND_BOOSTER_CREATE_DESC)
                .withArgument(ArgumentTypes.decimalAbs(CommandArguments.MULTIPLIER)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_MULTIPLIER)
                    .withSamples(tabContext -> Lists.newList("1.5", "2.0", "2.5", "3.0")))
                .withArgument(ArgumentTypes.integerAbs(CommandArguments.DURATION)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_DURATION)
                    .withSamples(tabContext -> Lists.newList("3600", "7200", "86400")))
                .withArgument(ArgumentTypes.playerName(CommandArguments.PLAYER))
                .executes((context, arguments) -> create(plugin, context, arguments))
            )
            .addDirect("activate", builder -> builder
                .description(Lang.COMMAND_BOOSTER_ACTIVATE_DESC)
                .withArgument(ArgumentTypes.string(CommandArguments.NAME)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_NAME)
                    .withSamples(tabContext -> new ArrayList<>(Config.getBoosterScheduleMap().keySet())))
                .executes((context, arguments) -> activate(plugin, context, arguments))
            )
            .addDirect("remove", builder -> builder
                .description(Lang.COMMAND_BOOSTER_REMOVE_DESC)
                .withArgument(ArgumentTypes.playerName(CommandArguments.PLAYER))
                .executes((context, arguments) -> remove(plugin, context, arguments))
            )
        );
    }

    private static boolean showBoosts(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context) {
        plugin.getBoosterManager().displayBoosterInfo(context.getPlayerOrThrow());
        return true;
    }

    private static boolean create(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        double modifier = arguments.getDoubleArgument(CommandArguments.MULTIPLIER);
        int duration = arguments.getIntArgument(CommandArguments.DURATION);
        Booster booster = Booster.create(modifier, duration);

        if (arguments.hasArgument(CommandArguments.PLAYER)) {
            String playerName = arguments.getStringArgument(CommandArguments.PLAYER);
            plugin.getUserManager().manageUser(playerName, user -> {
                if (user == null) {
                    context.errorBadPlayer();
                    return;
                }

                user.setBooster(booster);
                plugin.getUserManager().save(user);

                Player target = user.getPlayer();
                if (target != null) {
                    plugin.getBoosterManager().notifyPersonalBooster(target, booster);
                }

                Lang.COMMAND_BOOSTER_CREATE_DONE_PERSONAL.getMessage().send(context.getSender(), replacer -> replacer
                    .replace(Placeholders.PLAYER_NAME, user.getName())
                    .replace(Placeholders.GENERIC_AMOUNT, booster.formattedPercent())
                    .replace(Placeholders.GENERIC_TIME, TimeFormats.formatDuration(booster.getExpireDate(), TimeFormatType.LITERAL))
                );
            });
        }
        else {
            plugin.getBoosterManager().setGlobalBooster(booster);

            Lang.COMMAND_BOOSTER_CREATE_DONE_GLOBAL.getMessage().send(context.getSender(), replacer -> replacer
                .replace(Placeholders.GENERIC_AMOUNT, booster.formattedPercent())
                .replace(Placeholders.GENERIC_TIME, TimeFormats.formatDuration(booster.getExpireDate(), TimeFormatType.LITERAL))
            );
        }

        return true;
    }

    private static boolean activate(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String name = arguments.getStringArgument(CommandArguments.NAME);
        if (plugin.getBoosterManager().activateBoosterById(name)) {
            context.send(Lang.COMMAND_BOOSTER_ACTIVATE_DONE);
        }
        else {
            context.send(Lang.ERROR_INVALID_BOOSTER);
        }
        return true;
    }

    private static boolean remove(@NotNull LootConomyPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (arguments.hasArgument(CommandArguments.PLAYER)) {
            String playerName = arguments.getStringArgument(CommandArguments.PLAYER);
            plugin.getUserManager().manageUser(playerName, user -> {
                if (user == null) {
                    context.errorBadPlayer();
                    return;
                }

                if (!user.hasBooster()) {
                    context.send(Lang.COMMAND_BOOSTER_REMOVE_ERROR_NOTHING.getMessage());
                    return;
                }

                user.removeBooster();
                plugin.getUserManager().save(user);
                Lang.COMMAND_BOOSTER_REMOVE_DONE_PERSONAL.getMessage().send(context.getSender(), replacer -> replacer.replace(Placeholders.PLAYER_NAME, user.getName()));
            });
        }
        else {
            if (!plugin.getBoosterManager().hasGlobalBoost()) {
                context.send(Lang.COMMAND_BOOSTER_REMOVE_ERROR_NOTHING.getMessage());
                return false;
            }

            plugin.getBoosterManager().removeGlobalBooster();
            Lang.COMMAND_BOOSTER_REMOVE_DONE_GLOBAL.getMessage().send(context.getSender());
        }
        return true;
    }
}
