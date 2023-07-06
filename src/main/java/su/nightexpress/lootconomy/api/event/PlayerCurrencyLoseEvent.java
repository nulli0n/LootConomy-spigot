package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.skill.impl.SkillObjective;

public class PlayerCurrencyLoseEvent extends PlayerCurrencyEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerCurrencyLoseEvent(@NotNull Player player, @NotNull Currency currency, double money) {
        this(player, currency, money, null, null);
    }

    public PlayerCurrencyLoseEvent(@NotNull Player player, @NotNull Currency currency, double money, @Nullable Skill skill, @Nullable SkillObjective objective) {
        super(player, currency, money, skill, objective);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
