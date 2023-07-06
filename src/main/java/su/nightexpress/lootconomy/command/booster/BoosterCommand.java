package su.nightexpress.lootconomy.command.booster;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.config.Lang;

public class BoosterCommand extends GeneralCommand<LootConomy> {

    public BoosterCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"booster"}, Perms.COMMAND_BOOSTER);
        this.setDescription(plugin.getMessage(Lang.COMMAND_BOOSTER_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_BOOSTER_USAGE));

        this.addDefaultCommand(new HelpSubCommand<>(plugin));
        this.addChildren(new CreateSubCommand(plugin));
        this.addChildren(new ClearSubCommand(plugin));
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {

    }
}
