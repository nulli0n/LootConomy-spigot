package su.nightexpress.lootconomy.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.List;

public class ObjectivesCommand extends AbstractCommand<LootConomy> {

    public ObjectivesCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"objectives"}, Perms.COMMAND_OBJECTIVES);
        this.setDescription(plugin.getMessage(Lang.COMMAND_OBJECTIVES_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_OBJECTIVES_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return plugin.getSkillManager().getSkills().stream().filter(skill -> skill.hasPermission(player)).map(Skill::getId).toList();
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
        if (!skill.hasPermission(player)) {
            this.errorPermission(sender);
            return;
        }

        skill.getObjectivesMenu().open(player, 1);
    }
}
