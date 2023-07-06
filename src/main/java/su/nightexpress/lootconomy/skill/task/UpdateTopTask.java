package su.nightexpress.lootconomy.skill.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Pair;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateTopTask extends AbstractTask<LootConomy> {

    public UpdateTopTask(@NotNull LootConomy plugin) {
        super(plugin, Config.TOP_UPDATE_INTERVAL.get(), true);
    }

    @Override
    public void action() {
        Map<Skill, List<Pair<String, Integer>>> levelMap = this.plugin.getSkillManager().getTopLevelMap();
        Map<Skill, Map<String, Integer>> dataMap = this.plugin.getData().getLevels();

        levelMap.clear();
        dataMap.forEach((currency, users) -> {
            CollectionsUtil.sortDescent(users).forEach((name, balance) -> {
                levelMap.computeIfAbsent(currency, k -> new ArrayList<>()).add(Pair.of(name, balance));
            });
        });
    }
}
