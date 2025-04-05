package su.nightexpress.lootconomy.booster.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.booster.BoosterUtils;
import su.nightexpress.nightcore.util.TimeUtil;

public class Booster {

    private double multiplier;
    private long expireDate;

    public Booster(double multiplier, long expireDate) {
        this.setMultiplier(multiplier);
        this.setExpireDate(expireDate);
    }

    @NotNull
    public static Booster create(double multiplier, int duration) {
        return new Booster(multiplier, TimeUtil.createFutureTimestamp(duration));
    }

    public boolean isValid() {
        return this.multiplier != 0D && !this.isExpired();
    }

    public boolean isExpired() {
        return TimeUtil.isPassed(this.expireDate);
    }

    @NotNull
    public String formattedPercent() {
        return BoosterUtils.formatMultiplier(this.multiplier);
    }

    public double getAsPercent() {
        return BoosterUtils.getAsPercent(this.multiplier);
    }

    public double getMultiplier() {
        return this.multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = Math.abs(multiplier);
    }

    public long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }
}
