package su.nightexpress.lootconomy.data.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.lootconomy.currency.CurrencySettings;
import su.nightexpress.nightcore.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class LootLimitData {

    private final Map<String, Double> currencyEarned;
    private final long expireDate;

    @NotNull
    public static LootLimitData create() {
        return new LootLimitData(new HashMap<>(), generateExpireTimestamp());
    }

    public LootLimitData(@NotNull Map<String, Double> currencyEarned, long expireDate) {
        this.currencyEarned = currencyEarned;
        this.expireDate = expireDate;
    }

    public static long generateExpireTimestamp() {
        return TimeUtil.toEpochMillis(LocalDateTime.of(TimeUtil.getCurrentDate().plusDays(1), LocalTime.MIDNIGHT)) + 100L;
    }

    public boolean isLimitExceed(@NotNull Currency currency, @NotNull CurrencySettings settings) {
        return this.getCurrencyEarned(currency) >= settings.getDailyLimit();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= this.expireDate;
    }

    public void addCurrency(@NotNull Currency currency, double amount) {
        this.addCurrency(currency.getInternalId(), amount);
    }

    public void addCurrency(@NotNull String id, double amount) {
        if (amount <= 0D) return;

        this.currencyEarned.put(id.toLowerCase(), this.getCurrencyEarned(id) + amount);
    }

    public double getCurrencyEarned(@NotNull Currency currency) {
        return this.getCurrencyEarned(currency.getInternalId());
    }

    public double getCurrencyEarned(@NotNull String id) {
        return this.currencyEarned.getOrDefault(id.toLowerCase(), 0D);
    }

    public Map<String, Double> getCurrencyEarned() {
        return currencyEarned;
    }

    public long getExpireDate() {
        return expireDate;
    }
}
