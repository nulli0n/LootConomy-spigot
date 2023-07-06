package su.nightexpress.lootconomy.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.skill.impl.Rank;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InfoCommand extends AbstractCommand<LootConomy> {

    public InfoCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"info"}, Perms.COMMAND_INFO);
        this.setDescription(plugin.getMessage(Lang.COMMAND_INFO_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_INFO_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return new ArrayList<>(plugin.getSkillManager().getSkillMap().keySet());
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2) {
            this.printUsage(sender);
            return;
        }

        Skill skill = plugin.getSkillManager().getSkillById(result.getArg(1));
        if (skill == null) {
            plugin.getMessage(Lang.SKILL_ERROR_INVALID).send(sender);
            return;
        }

        Player player = (Player) sender;
        LootUser user = plugin.getUserManager().getUserData(player);
        Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player, skill);
        SkillData data = user.getData(skill);
        Rank rank = data.getRank();
        int level = data.getLevel();
        ExpirableBooster booster = user.getBooster(skill);

        plugin.getMessage(Lang.COMMAND_INFO_DISPLAY)
            .replace(data.replacePlaceholders())
            .replace(Placeholders.XP_MULTIPLIER, NumberUtil.format(rank.getXPMultiplier(level)))
            .replace(Placeholders.XP_BOOST_MODIFIER, NumberUtil.format(Booster.getXPBoost(boosters)))
            .replace(Placeholders.XP_BOOST_PERCENT, NumberUtil.format(Booster.getXPPercent(boosters)))
            .replace(Placeholders.BOOSTER_TIME_LEFT, booster == null ? "-" : TimeUtil.formatTimeLeft(booster.getExpireDate()))
            .replace(str -> str.contains(Placeholders.CURRENCY_MULTIPLIER), (line, list) -> {
                plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
                    double multiplier = rank.getCurrencyMultiplier(currency, level);
                    list.add(currency.replacePlaceholders().apply(line)
                        .replace(Placeholders.CURRENCY_MULTIPLIER, NumberUtil.format(multiplier))
                    );
                });
            })
            .replace(str -> str.contains(Placeholders.CURRENCY_BOOST_PERCENT) || str.contains(Placeholders.CURRENCY_BOOST_MODIFIER), (line, list) -> {
                plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
                    double percent = Booster.getCurrencyPercent(currency, boosters);
                    double modifier = Booster.getCurrencyBoost(currency, boosters);
                    list.add(currency.replacePlaceholders().apply(line)
                        .replace(Placeholders.CURRENCY_BOOST_PERCENT, NumberUtil.format(percent))
                        .replace(Placeholders.CURRENCY_BOOST_MODIFIER, NumberUtil.format(modifier))
                    );
                });
            })
            .send(player);
    }
}
