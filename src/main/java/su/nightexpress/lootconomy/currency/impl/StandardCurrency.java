package su.nightexpress.lootconomy.currency.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.api.currency.CurrencyHandler;
import su.nightexpress.lootconomy.currency.CurrencySettings;
import su.nightexpress.lootconomy.money.object.DeathPenalty;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

public class StandardCurrency<T extends CurrencyHandler> implements Currency {

    protected final String           id;
    protected final T                handler;
    protected final CurrencySettings settings;
    protected final PlaceholderMap   placeholderMap;

    public StandardCurrency(@NotNull String id, @NotNull T handler, @NotNull CurrencySettings settings) {
        this.handler = handler;
        this.id = id.toLowerCase();
        this.settings = settings;
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.CURRENCY_ID, this::getId)
            .add(Placeholders.CURRENCY_NAME, this::getName);
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    public CurrencySettings getSettings() {
        return settings;
    }

    @NotNull
    @Override
    public T getHandler() {
        return handler;
    }

    @Override
    public boolean isEnabled() {
        return this.settings.isEnabled();
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public String getName() {
        return this.settings.getName();
    }

    @NotNull
    @Override
    public String getFormat() {
        return this.settings.getFormat();
    }

    @Override
    @NotNull
    public String getDropFormat() {
        return this.settings.getDropFormat();
    }

    @Override
    public boolean isDirectToBalance() {
        return this.settings.isDirectToBalance();
    }

    @Override
    public double getDailyLimit() {
        return this.settings.getDailyLimit();
    }

    @NotNull
    @Override
    public UniParticle getGroundEffect() {
        return this.settings.getGroundEffect();
    }

    @NotNull
    @Override
    public UniSound getPickupSound() {
        return this.settings.getPickupSound();
    }

    @Override
    @NotNull
    public DeathPenalty getDeathPenalty() {
        return this.settings.getDeathPenalty();
    }

    @Override
    @NotNull
    public ItemStack getIcon(double amount) {
        return this.settings.getIcon(amount);
    }
}
