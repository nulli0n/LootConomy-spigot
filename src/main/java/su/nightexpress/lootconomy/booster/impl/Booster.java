package su.nightexpress.lootconomy.booster.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.booster.BoosterMultiplier;
import su.nightexpress.lootconomy.booster.config.BoosterInfo;

import java.util.Collection;

public class Booster {

    private final BoosterMultiplier multiplier;

    public Booster(@NotNull BoosterInfo parent) {
        this(parent.getMultiplier());
    }

    public Booster(@NotNull BoosterMultiplier multiplier) {
        this.multiplier = multiplier;
    }

    @NotNull
    public BoosterMultiplier getMultiplier() {
        return this.multiplier;
    }

    public static double getCurrencyBoost(@NotNull Currency currency, @NotNull Collection<Booster> boosters) {
        return getCurrencyBoost(currency.getId(), boosters);
    }

    public static double getCurrencyBoost(@NotNull String id, @NotNull Collection<Booster> boosters) {
        return (boosters.stream().mapToDouble(b -> b.getMultiplier().getCurrencyPercent(id)).sum() + 100D) / 100D;
    }

    public static double getXPBoost(@NotNull Collection<Booster> boosters) {
        return (boosters.stream().mapToDouble(b -> b.getMultiplier().getXPPercent()).sum() + 100D) / 100D;
    }

    public static double getCurrencyPercent(@NotNull Currency currency, @NotNull Collection<Booster> boosters) {
        return getCurrencyPercent(currency.getId(), boosters);
    }

    public static double getCurrencyPercent(@NotNull String id, @NotNull Collection<Booster> boosters) {
        return boosters.stream().mapToDouble(b -> b.getMultiplier().getCurrencyPercent(id)).sum();
    }

    public static double getXPPercent(@NotNull Collection<Booster> boosters) {
        return boosters.stream().mapToDouble(b -> b.getMultiplier().getXPPercent()).sum();
    }
}
