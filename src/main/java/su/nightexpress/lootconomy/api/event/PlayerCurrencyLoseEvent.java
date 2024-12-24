package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.lootconomy.money.object.MoneyObjective;

public class PlayerCurrencyLoseEvent extends PlayerCurrencyEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerCurrencyLoseEvent(@NotNull Player player, @NotNull Currency currency, double money) {
        this(player, currency, money, null);
    }

    public PlayerCurrencyLoseEvent(@NotNull Player player, @NotNull Currency currency, double money, @Nullable MoneyObjective objective) {
        super(player, currency, money, objective);
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
