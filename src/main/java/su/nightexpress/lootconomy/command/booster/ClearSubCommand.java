package su.nightexpress.lootconomy.command.booster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.ArrayList;
import java.util.List;

class ClearSubCommand extends AbstractCommand<LootConomy> {

    public ClearSubCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"clear"}, Perms.COMMAND_BOOSTER);
        this.setDescription(plugin.getMessage(Lang.COMMAND_BOOSTER_CLEAR_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_BOOSTER_CLEAR_USAGE));
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 2) {
            return CollectionsUtil.playerNames(player);
        }
        if (arg == 3) {
            return new ArrayList<>(plugin.getSkillManager().getSkillMap().keySet());
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 3) {
            this.printUsage(sender);
            return;
        }

        this.plugin.getUserManager().getUserDataAsync(result.getArg(2)).thenAccept(user -> {
            if (user == null) {
                this.errorPlayer(sender);
                return;
            }

            if (result.length() >= 3) {
                Skill skill = this.plugin.getSkillManager().getSkillById(result.getArg(3));
                if (skill == null) {
                    this.plugin.getMessage(Lang.SKILL_ERROR_INVALID).send(sender);
                    return;
                }
                user.getBoosterMap().remove(skill.getId());

                this.plugin.getMessage(Lang.COMMAND_BOOSTER_CLEAR_DONE_SKILL)
                    .replace(skill.replacePlaceholders())
                    .replace(Placeholders.PLAYER_NAME, user.getName())
                    .send(sender);
            }
            else {
                user.getBoosterMap().clear();

                this.plugin.getMessage(Lang.COMMAND_BOOSTER_CLEAR_DONE_ALL)
                    .replace(Placeholders.PLAYER_NAME, user.getName())
                    .send(sender);
            }
            this.plugin.getUserManager().saveUser(user);
        });
    }
}
