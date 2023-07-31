package su.nightexpress.lootconomy.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;

public class SkillsCommand extends GeneralCommand<LootConomy> {

    public SkillsCommand(@NotNull LootConomy plugin, @NotNull String name) {
        super(plugin, new String[]{name}, Perms.COMMAND_SKILLS);
        this.setDescription(plugin.getMessage(Lang.COMMAND_SKILLS_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_SKILLS_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        plugin.getSkillManager().getSkillListMenu().open(player, 1);
    }
}
