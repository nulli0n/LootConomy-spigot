package su.nightexpress.lootconomy.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.lang.LangManager;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.data.impl.LootUser;

public class SoundCommand extends AbstractCommand<LootConomy> {

    public SoundCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"sound"}, Perms.COMMAND_SOUND);
        this.setDescription(plugin.getMessage(Lang.COMMAND_SOUND_DESC));
        this.setPlayerOnly(true);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        LootUser user = plugin.getUserManager().getUserData(player);

        user.getSettings().setPickupSound(!user.getSettings().isPickupSound());
        this.plugin.getUserManager().saveUser(user);

        plugin.getMessage(Lang.COMMAND_SOUND_DONE)
            .replace(Placeholders.GENERIC_STATE, LangManager.getBoolean(user.getSettings().isPickupSound()))
            .send(player);
    }
}
