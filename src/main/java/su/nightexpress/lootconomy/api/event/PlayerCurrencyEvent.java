package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.skill.impl.SkillObjective;

public abstract class PlayerCurrencyEvent extends Event implements Cancellable {

    protected final Player player;

    protected boolean        isCancelled;
    protected double         amount;
    protected Currency currency;
    protected Skill          skill;
    protected SkillObjective objective;

    public PlayerCurrencyEvent(@NotNull Player player, @NotNull Currency currency, double amount) {
        this(player, currency, amount, null, null);
    }

    public PlayerCurrencyEvent(@NotNull Player player, @NotNull Currency currency, double amount,
                               @Nullable Skill skill, @Nullable SkillObjective objective) {
        this.player = player;
        this.setAmount(amount);
        this.setCurrency(currency);
        this.setSkill(skill);
        this.setObjective(objective);
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled || this.getAmount() == 0D;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
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

    public void setSkill(@Nullable Skill skill) {
        this.skill = skill;
    }

    @Nullable
    public Skill getSkill() {
        return skill;
    }

    @Nullable
    public SkillObjective getObjective() {
        return objective;
    }

    public void setObjective(@Nullable SkillObjective objective) {
        this.objective = objective;
    }
}
