package su.nightexpress.lootconomy.booster.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.config.ScheduledBoosterInfo;

public class ExpirableBooster extends Booster {

    private long expireDate;

    public ExpirableBooster(@NotNull ScheduledBoosterInfo parent) {
        this(parent.getMultiplier(), parent.getDuration());
    }

    public ExpirableBooster(@NotNull Multiplier multiplier, int duration) {
        this(multiplier, System.currentTimeMillis() + duration * 1000L + 100L);
    }

    public ExpirableBooster(@NotNull Multiplier multiplier, long expireDate) {
        super(multiplier);
        this.setExpireDate(expireDate);
    }

    public boolean isExpired() {
        return this.expireDate > 0L && System.currentTimeMillis() > this.expireDate;
    }

    public long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }
}
