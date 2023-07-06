package su.nightexpress.lootconomy.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.Pair;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TopCommand extends AbstractCommand<LootConomy> {

    public TopCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"top"}, Perms.COMMAND_TOP);
        this.setDescription(plugin.getMessage(Lang.COMMAND_TOP_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_TOP_USAGE));
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return new ArrayList<>(plugin.getSkillManager().getSkillMap().keySet());
        }
        if (arg == 2) {
            return Arrays.asList("1", "2", "3", "4", "5", "10", "20");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2) {
            this.printUsage(sender);
            return;
        }

        Skill skill = this.plugin.getSkillManager().getSkillById(result.getArg(1));
        if (skill == null) {
            this.plugin.getMessage(Lang.SKILL_ERROR_INVALID).send(sender);
            return;
        }

        int perPage = Config.TOP_ENTRIES_PER_PAGE.get();

        List<Pair<String, Integer>> full = this.plugin.getSkillManager().getTopLevelMap().getOrDefault(skill, Collections.emptyList());
        List<List<Pair<String, Integer>>> split = CollectionsUtil.split(full, perPage);
        int pages = split.size();
        int page = Math.max(0, Math.min(pages, Math.abs(result.getInt(2, 1))) - 1);

        List<Pair<String, Integer>> list = pages > 0 ? split.get(page) : new ArrayList<>();
        AtomicInteger pos = new AtomicInteger(1 + perPage * page);

        this.plugin.getMessage(Lang.COMMAND_TOP_LIST)
            .replace(skill.replacePlaceholders())
            .replace(Placeholders.GENERIC_CURRENT, page + 1)
            .replace(Placeholders.GENERIC_MAX, pages)
            .replace(str -> str.contains(Placeholders.GENERIC_AMOUNT), (line, list1) -> {
                for (Pair<String, Integer> pair : list) {
                    list1.add(line
                        .replace(Placeholders.GENERIC_POS, NumberUtil.format(pos.getAndIncrement()))
                        .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(pair.getSecond()))
                        .replace(Placeholders.Player.NAME, pair.getFirst()));
                }
            })
            .send(sender);
    }
}
