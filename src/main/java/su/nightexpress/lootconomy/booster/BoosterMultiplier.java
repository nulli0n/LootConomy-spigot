package su.nightexpress.lootconomy.booster;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.lootconomy.api.currency.Currency;

import java.util.HashMap;
import java.util.Map;

public class BoosterMultiplier {

    private final Map<String, Double> currencyMultiplier;
    private final double              xpMultiplier;

    public BoosterMultiplier(@NotNull Map<String, Double> currencyMultiplier, double xpMultiplier) {
        this.currencyMultiplier = currencyMultiplier;
        this.xpMultiplier = xpMultiplier;
    }

    @NotNull
    public static BoosterMultiplier read(@NotNull JYML cfg, @NotNull String path) {
        Map<String, Double> currency = new HashMap<>();
        for (String curId : cfg.getSection(path + ".Multiplier.Currency")) {
            double mod = cfg.getDouble(path + ".Multiplier.Currency." + curId, 1D);

            currency.put(curId.toLowerCase(), mod);
        }

        double xpModifier = cfg.getDouble(path + ".Multiplier.XP", 1D);
        return new BoosterMultiplier(currency, xpModifier);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        this.getCurrencyMultiplier().forEach((curId, mod) -> {
            cfg.set(path + ".Multiplier.Currency." + curId, mod);
        });
        cfg.set(path + ".Multiplier.XP", this.getXPMultiplier());
    }

    public double getCurrencyMultiplier(@NotNull Currency currency) {
        return this.getCurrencyMultiplier(currency.getId());
    }

    public double getCurrencyMultiplier(@NotNull String curId) {
        return this.getCurrencyMultiplier().getOrDefault(curId.toLowerCase(), 1D);
    }

    public double getCurrencyPercent(@NotNull Currency currency) {
        return this.getCurrencyPercent(currency.getId());
    }

    public double getCurrencyPercent(@NotNull String curId) {
        return this.getCurrencyMultiplier(curId) * 100D - 100D;
    }

    public double getXPPercent() {
        return this.getXPMultiplier() * 100D - 100D;
    }

    @NotNull
    public Map<String, Double> getCurrencyMultiplier() {
        return currencyMultiplier;
    }

    public double getXPMultiplier() {
        return xpMultiplier;
    }
}
