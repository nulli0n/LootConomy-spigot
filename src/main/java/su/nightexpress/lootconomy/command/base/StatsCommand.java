package su.nightexpress.lootconomy.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.message.NexParser;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.config.Lang;

import java.util.List;

public class StatsCommand extends AbstractCommand<LootConomy> {

    public StatsCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"stats"}, Perms.COMMAND_STATS);
        this.setDescription(plugin.getMessage(Lang.COMMAND_STATS_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_STATS_USAGE));
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        this.plugin.getUserManager().getUserDataAsync(result.getArg(1, sender.getName())).thenAccept(user -> {
            if (user == null) {
                this.errorPlayer(sender);
                return;
            }

            plugin.getMessage(Lang.COMMAND_STATS_DISPLAY)
                .replace(Placeholders.Player.NAME, user.getName())
                .replace(str -> str.contains(Placeholders.SKILL_NAME), (line, list) -> {
                    this.plugin.getSkillManager().getSkills().forEach(skill -> {
                        list.add(user.getData(skill).replacePlaceholders().apply(line).replace("\n", NexParser.TAG_NEWLINE));
                    });
                })
                .send(sender);
        });
    }
}
