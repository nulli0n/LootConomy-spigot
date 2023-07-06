package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.skill.impl.SkillObjective;

public class PlayerCurrencyGainEvent extends PlayerCurrencyEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerCurrencyGainEvent(@NotNull Player player, @NotNull Currency currency, double amount) {
        this(player, currency, amount, null, null);
    }

    public PlayerCurrencyGainEvent(@NotNull Player player, @NotNull Currency currency, double amount,
                                   @Nullable Skill skill, @Nullable SkillObjective objective) {
        super(player, currency, amount, skill, objective);
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
