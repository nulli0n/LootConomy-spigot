package su.nightexpress.lootconomy.data.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SkillLimitData {

    private final String              jobId;
    private final Map<String, Double> currencyEarned;

    private double xpEarned;
    private long   since;

    @NotNull
    public static SkillLimitData create(@NotNull Skill job) {
        return new SkillLimitData(job.getId(), new HashMap<>(), 0, System.currentTimeMillis());
    }

    public SkillLimitData(@NotNull String jobId, @NotNull Map<String, Double> currencyEarned, double xpEarned, long since) {
        this.jobId = jobId.toLowerCase();
        this.currencyEarned = currencyEarned;
        this.xpEarned = xpEarned;
        this.since = since;
    }

    public void checkExpiration() {
        long minDiff = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        if (System.currentTimeMillis() - this.getSince() < minDiff) return;

        this.since = System.currentTimeMillis();
        this.getCurrencyEarned().clear();
        this.setXPEarned(0D);
    }

    public void addCurrency(@NotNull Currency currency, double amount) {
        this.addCurrency(currency.getId(), amount);
    }

    public void addCurrency(@NotNull String id, double amount) {
        if (amount <= 0D) return;

        this.getCurrencyEarned().put(id.toLowerCase(), this.getCurrencyEarned(id) + amount);
    }

    public void addXP(int amount) {
        if (amount <= 0) return;

        this.setXPEarned(this.getXPEarned() + amount);
    }

    public double getCurrencyEarned(@NotNull Currency currency) {
        return this.getCurrencyEarned(currency.getId());
    }

    public double getCurrencyEarned(@NotNull String id) {
        return this.getCurrencyEarned().getOrDefault(id.toLowerCase(), 0D);
    }

    @NotNull
    public String getJobId() {
        return jobId;
    }

    public Map<String, Double> getCurrencyEarned() {
        return currencyEarned;
    }

    public double getXPEarned() {
        return xpEarned;
    }

    public void setXPEarned(double xpEarned) {
        this.xpEarned = xpEarned;
    }

    public long getSince() {
        return since;
    }
}
