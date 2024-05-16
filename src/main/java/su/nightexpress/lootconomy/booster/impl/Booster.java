package su.nightexpress.lootconomy.booster.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.config.BoosterInfo;

import java.util.Collection;

public class Booster {

    private final Multiplier multiplier;

    public Booster(@NotNull BoosterInfo parent) {
        this(parent.getMultiplier());
    }

    public Booster(@NotNull Multiplier multiplier) {
        this.multiplier = multiplier;
    }

    @NotNull
    public Multiplier getMultiplier() {
        return this.multiplier;
    }

    public static double getMultiplier(@NotNull Currency currency, @NotNull Collection<Booster> boosters) {
        return getMultiplier(currency.getId(), boosters);
    }

    public static double getMultiplier(@NotNull String id, @NotNull Collection<Booster> boosters) {
        return (boosters.stream().mapToDouble(b -> b.getMultiplier().getAsPercent(id)).sum() + 100D) / 100D;
    }

    public static double getPercent(@NotNull Currency currency, @NotNull Collection<Booster> boosters) {
        return getPercent(currency.getId(), boosters);
    }

    public static double getPercent(@NotNull String id, @NotNull Collection<Booster> boosters) {
        return boosters.stream().mapToDouble(b -> b.getMultiplier().getAsPercent(id)).sum();
    }
}
