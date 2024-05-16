package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.money.object.MoneyObjective;

public abstract class PlayerCurrencyEvent extends Event implements Cancellable {

    protected final Player player;

    protected boolean        cancelled;
    protected double         amount;
    protected Currency       currency;
    protected MoneyObjective objective;

    public PlayerCurrencyEvent(@NotNull Player player, @NotNull Currency currency, double amount) {
        this(player, currency, amount, null);
    }

    public PlayerCurrencyEvent(@NotNull Player player, @NotNull Currency currency, double amount, @Nullable MoneyObjective objective) {
        this.player = player;
        this.setAmount(amount);
        this.setCurrency(currency);
        this.setObjective(objective);
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled || this.getAmount() == 0D;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = Math.abs(amount);
    }

    @NotNull
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(@NotNull Currency currency) {
        this.currency = currency;
    }

    @Nullable
    public MoneyObjective getObjective() {
        return objective;
    }

    public void setObjective(@Nullable MoneyObjective objective) {
        this.objective = objective;
    }
}
