package su.nightexpress.lootconomy.hook.impl;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlaceholderHook {

    private static Expansion expansion;

    public static void setup(@NotNull LootConomy plugin) {
        if (expansion == null) {
            expansion = new Expansion(plugin);
            expansion.register();
        }
    }

    public static void shutdown() {
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
    }

    static class Expansion extends PlaceholderExpansion {

        private final LootConomy plugin;

        public Expansion(@NotNull LootConomy plugin) {
            this.plugin = plugin;
        }

        @Override
        @NotNull
        public String getAuthor() {
            return this.plugin.getDescription().getAuthors().get(0);
        }

        @Override
        @NotNull
        public String getIdentifier() {
            return this.plugin.getName().toLowerCase();
        }

        @Override
        @NotNull
        public String getVersion() {
            return this.plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) return null;

            String key = params.split("_")[0];
            String rest = params.substring(key.length() + 1);

            Skill skill = plugin.getSkillManager().getSkillById(key);
            if (skill != null) {
                LootUser user = this.plugin.getUserManager().getUserData(player);
                SkillData data = user.getData(skill);

                if (rest.equalsIgnoreCase("level")) {
                    return NumberUtil.format(data.getLevel());
                }
                if (rest.equalsIgnoreCase("xp")) {
                    return NumberUtil.format(data.getXP());
                }
                if (rest.equalsIgnoreCase("xp_required")) {
                    return NumberUtil.format(data.getMaxXP());
                }
                if (rest.equalsIgnoreCase("xp_to_up")) {
                    return NumberUtil.format(data.getXPToLevelUp());
                }
                if (rest.equalsIgnoreCase("xp_to_down")) {
                    return NumberUtil.format(data.getXPToLevelDown());
                }
                if (rest.equalsIgnoreCase("xp_multiplier")) {
                    return NumberUtil.format(data.getRank().getXPMultiplier(data.getLevel()));
                }
                if (rest.equalsIgnoreCase("xp_boost_multiplier")) {
                    Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player, skill);
                    return NumberUtil.format(Booster.getXPBoost(boosters));
                }
                if (rest.equalsIgnoreCase("xp_boost_percent")) {
                    Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player, skill);
                    return NumberUtil.format(Booster.getXPPercent(boosters));
                }
                if (rest.startsWith("currency_multiplier_")) {
                    String curId = rest.substring("currency_multiplier_".length());
                    return NumberUtil.format(data.getRank().getCurrencyMultiplier(curId, data.getLevel()));
                }
                if (rest.startsWith("currency_boost_multiplier_")) {
                    String curId = rest.substring("currency_boost_multiplier_".length());
                    Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player, skill);
                    return NumberUtil.format(Booster.getCurrencyBoost(curId, boosters));
                }
                if (rest.startsWith("currency_boost_percent_")) {
                    String curId = rest.substring("currency_boost_percent_".length());
                    Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player, skill);
                    return NumberUtil.format(Booster.getCurrencyPercent(curId, boosters));
                }
                if (rest.startsWith("top_level_")) {
                    String[] info = rest.substring("top_level_".length()).split("_");
                    if (info.length < 2) return null;

                    int pos = StringUtil.getInteger(info[0], 0);
                    String type = info[1];

                    List<Pair<String, Integer>> list = this.plugin.getSkillManager().getTopLevelMap()
                        .getOrDefault(skill, Collections.emptyList());
                    if (list.size() <= pos) return "-";

                    var pair = list.get(pos - 1);
                    if (type.equalsIgnoreCase("name")) {
                        return pair.getFirst();
                    }
                    else if (type.equalsIgnoreCase("value")) {
                        return NumberUtil.format(pair.getSecond());
                    }
                }
            }
            return null;
        }
    }
}
