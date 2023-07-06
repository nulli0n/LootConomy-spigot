package su.nightexpress.lootconomy.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.command.CommandFlags;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.ArrayList;
import java.util.List;

public class ResetCommand extends AbstractCommand<LootConomy> {

    public ResetCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"reset"}, Perms.COMMAND_RESET);
        this.setDescription(plugin.getMessage(Lang.COMMAND_RESET_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_RESET_USAGE));
        this.addFlag(CommandFlags.SILENT);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return new ArrayList<>(plugin.getSkillManager().getSkillMap().keySet());
        }
        if (arg == 2 && player.hasPermission(Perms.COMMAND_RESET_OTHERS)) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2) {
            this.printUsage(sender);
            return;
        }
        if (result.length() >= 3 && !sender.hasPermission(Perms.COMMAND_RESET_OTHERS)) {
            this.errorPermission(sender);
            return;
        }

        Skill skill = plugin.getSkillManager().getSkillById(result.getArg(1));
        if (skill == null) {
            plugin.getMessage(Lang.SKILL_ERROR_INVALID).send(sender);
            return;
        }

        LootUser user = plugin.getUserManager().getUserData(result.getArg(2, sender.getName()));
        if (user == null) {
            this.errorPlayer(sender);
            return;
        }

        SkillData skillData = user.getData(skill);
        skillData.reset();

        if (!sender.getName().equalsIgnoreCase(user.getName())) {
            plugin.getMessage(Lang.COMMAND_RESET_DONE)
                .replace(skillData.replacePlaceholders())
                .replace(Placeholders.Player.NAME, user.getName())
                .send(sender);
        }

        Player target = user.getPlayer();
        if (target != null && !result.hasFlag(CommandFlags.SILENT)) {
            plugin.getMessage(Lang.SKILL_RESET_NOTIFY).replace(skillData.replacePlaceholders()).send(target);
        }
    }
}
