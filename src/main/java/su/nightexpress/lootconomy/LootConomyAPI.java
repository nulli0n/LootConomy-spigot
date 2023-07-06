package su.nightexpress.lootconomy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.skill.SkillManager;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.money.MoneyManager;

import java.util.Collection;

public class LootConomyAPI {

    public static final LootConomy PLUGIN = LootConomy.getPlugin(LootConomy.class);

    @NotNull
    public static LootUser getUserData(@NotNull Player player) {
        return PLUGIN.getUserManager().getUserData(player);
    }

    @Nullable
    public static Currency getCurrency(@NotNull String id) {
        return PLUGIN.getCurrencyManager().getCurrency(id);
    }

    @NotNull
    public static MoneyManager getMoneyManager() {
        return PLUGIN.getMoneyManager();
    }

    @NotNull
    public static SkillManager getSkillManager() {
        return PLUGIN.getSkillManager();
    }

    @Nullable
    public static Skill getSkillById(@NotNull String id) {
        return PLUGIN.getSkillManager().getSkillById(id);
    }

    @NotNull
    public static Collection<Skill> getSkills() {
        return PLUGIN.getSkillManager().getSkills();
    }
}
