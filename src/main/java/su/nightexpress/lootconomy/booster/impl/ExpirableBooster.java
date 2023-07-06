package su.nightexpress.lootconomy.booster.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.booster.BoosterMultiplier;
import su.nightexpress.lootconomy.booster.config.TimedBoosterInfo;

public class ExpirableBooster extends Booster {

    private final long expireDate;

    public ExpirableBooster(@NotNull TimedBoosterInfo parent) {
        this(parent.getMultiplier(), parent.getDuration());
    }

    public ExpirableBooster(@NotNull BoosterMultiplier multiplier, int duration) {
        this(multiplier, System.currentTimeMillis() + duration * 1000L + 100L);
    }

    public ExpirableBooster(@NotNull BoosterMultiplier multiplier, long expireDate) {
        super(multiplier);
        this.expireDate = expireDate;
    }

    public boolean isExpired() {
        return this.expireDate > 0L && System.currentTimeMillis() > this.expireDate;
    }

    public long getExpireDate() {
        return expireDate;
    }
}
