package su.nightexpress.lootconomy.command.booster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.booster.config.BoosterInfo;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.command.CommandFlags;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.*;
import java.util.stream.Collectors;

class CreateSubCommand extends AbstractCommand<LootConomy> {

    public CreateSubCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"create"}, Perms.COMMAND_BOOSTER);
        this.setDescription(plugin.getMessage(Lang.COMMAND_BOOSTER_CREATE_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_BOOSTER_CREATE_USAGE));
        this.addFlag(CommandFlags.SILENT);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 2) {
            return CollectionsUtil.playerNames(player);
        }
        if (arg == 3) {
            return new ArrayList<>(Config.BOOSTERS_CUSTOM.get().keySet());
        }
        if (arg == 4) {
            return Arrays.asList("600", "3600", "7200", "86400");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 5) {
            this.printUsage(sender);
            return;
        }

        BoosterInfo boosterInfo = Config.BOOSTERS_CUSTOM.get().get(result.getArg(3));
        if (boosterInfo == null) {
            this.plugin.getMessage(Lang.BOOSTER_ERROR_INVALID).send(sender);
            return;
        }

        Set<Skill> skills = new HashSet<>();
        if (boosterInfo.getSkills().contains(Placeholders.WILDCARD)) {
            skills.addAll(plugin.getSkillManager().getSkills());
        }
        else {
            skills.addAll(boosterInfo.getSkills().stream().map(id -> plugin.getSkillManager().getSkillById(id)).filter(Objects::nonNull).collect(Collectors.toSet()));
        }

        if (skills.isEmpty()) {
            this.plugin.getMessage(Lang.BOOSTER_ERROR_INVALID).send(sender);
            return;
        }

        int duration = result.getInt(4, 0);
        if (duration <= 0) return;

        plugin.getUserManager().getUserDataAsync(result.getArg(2)).thenAccept(user -> {
            if (user == null) {
                this.errorPlayer(sender);
                return;
            }

            ExpirableBooster booster = new ExpirableBooster(boosterInfo.getMultiplier(), duration);
            skills.forEach(skill -> {
                user.getBoosterMap().put(skill.getId(), booster);
            });
            user.saveData(this.plugin);

            this.plugin.getMessage(Lang.COMMAND_BOOSTER_CREATE_DONE)
                .replace(Placeholders.GENERIC_NAME, result.getArg(2))
                .replace(Placeholders.GENERIC_TIME, TimeUtil.formatTimeLeft(booster.getExpireDate()))
                .replace(Placeholders.Player.NAME, user.getName())
                .send(sender);

            Player player = user.getPlayer();
            if (player != null && !result.hasFlag(CommandFlags.SILENT)) {
                skills.forEach(skill -> {
                    this.plugin.getMessage(Lang.COMMAND_BOOSTER_CREATE_NOTIFY)
                        .replace(skill.replacePlaceholders())
                        .replace(Placeholders.GENERIC_TIME, TimeUtil.formatTimeLeft(booster.getExpireDate()))
                        .send(player);
                });
            }
        });
    }
}
