package su.nightexpress.lootconomy.booster;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.HashMap;
import java.util.Map;

public class Multiplier {

    private final Map<String, Double> currencyMap;

    public Multiplier() {
        this(new HashMap<>());
    }

    public Multiplier(@NotNull Map<String, Double> currencyMap) {
        this.currencyMap = currencyMap;
    }

    @NotNull
    public static Multiplier read(@NotNull FileConfig config, @NotNull String path) {
        Map<String, Double> currencyMap = new HashMap<>();
        for (String curId : config.getSection(path + ".Currency")) {
            double mod = config.getDouble(path + ".Currency." + curId, 1D);

            currencyMap.put(curId.toLowerCase(), mod);
        }
        return new Multiplier(currencyMap);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        this.currencyMap.forEach((curId, mod) -> {
            config.set(path + ".Currency." + curId, mod);
        });
    }

    @NotNull
    public Multiplier withCurrency(@NotNull Currency currency, double multiplier) {
        this.currencyMap.put(currency.getInternalId(), multiplier);
        return this;
    }

    public boolean has(@NotNull Currency currency) {
        return this.currencyMap.containsKey(currency.getInternalId());
    }

    public double getMultiplier(@NotNull Currency currency) {
        return this.getMultiplier(currency.getInternalId());
    }

    public double getMultiplier(@NotNull String curId) {
        return this.getCurrencyMap().getOrDefault(curId.toLowerCase(), 1D);
    }

    public double getAsPercent(@NotNull Currency currency) {
        return this.getAsPercent(currency.getInternalId());
    }

    public double getAsPercent(@NotNull String curId) {
        return this.getMultiplier(curId) * 100D - 100D;
    }

    @NotNull
    public String formattedPercent(@NotNull Currency currency) {
        return BoosterManager.formatBoosterValue(this.getAsPercent(currency));
    }

    @NotNull
    public String formattedMultiplier(@NotNull Currency currency) {
        return NumberUtil.format(this.getMultiplier(currency));
    }

    @NotNull
    public Map<String, Double> getCurrencyMap() {
        return currencyMap;
    }
}
