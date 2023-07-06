package su.nightexpress.lootconomy.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.command.CommandFlags;
import su.nightexpress.lootconomy.command.CommandMode;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XPCommand extends AbstractCommand<LootConomy> {

    public XPCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"xp"}, Perms.COMMAND_XP);
        this.setDescription(plugin.getMessage(Lang.COMMAND_XP_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_XP_USAGE));
        this.addFlag(CommandFlags.SILENT);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return CollectionsUtil.getEnumsList(CommandMode.class).stream().map(String::toLowerCase).toList();
        }
        if (arg == 2) {
            return new ArrayList<>(plugin.getSkillManager().getSkillMap().keySet());
        }
        if (arg == 3) {
            return Arrays.asList("10", "50", "100");
        }
        if (arg == 4) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 4) {
            this.printUsage(sender);
            return;
        }

        Skill skill = plugin.getSkillManager().getSkillById(result.getArg(2));
        if (skill == null) {
            plugin.getMessage(Lang.SKILL_ERROR_INVALID).send(sender);
            return;
        }

        CommandMode mode = StringUtil.getEnum(result.getArg(1), CommandMode.class).orElse(CommandMode.SET);

        int amount = Math.abs(result.getInt(3, 0));
        if (amount == 0) {
            this.errorNumber(sender, result.getArg(3));
            return;
        }

        LootUser user = plugin.getUserManager().getUserData(result.getArg(4, sender.getName()));
        if (user == null) {
            this.errorPlayer(sender);
            return;
        }

        SkillData skillData = user.getData(skill);
        skillData.setXP(mode.modify(skillData.getXP(), amount));
        skillData.normalize();

        plugin.getMessage(switch (mode) {
            case ADD -> Lang.COMMAND_XP_ADD_DONE;
            case REMOVE -> Lang.COMMAND_XP_REMOVE_DONE;
            case SET -> Lang.COMMAND_XP_SET_DONE;
        })
            .replace(skillData.replacePlaceholders())
            .replace(Placeholders.Player.NAME, user.getName())
            .replace(Placeholders.GENERIC_AMOUNT, amount)
            .send(sender);

        Player target = user.getPlayer();
        if (target != null && !result.hasFlag(CommandFlags.SILENT)) {
            plugin.getMessage(switch (mode) {
                case ADD -> Lang.COMMAND_XP_ADD_NOTIFY;
                case REMOVE -> Lang.COMMAND_XP_REMOVE_NOTIFY;
                case SET -> Lang.COMMAND_XP_SET_NOTIFY;
            })
                .replace(skillData.replacePlaceholders())
                .replace(Placeholders.GENERIC_AMOUNT, amount)
                .send(target);
        }
    }
}
