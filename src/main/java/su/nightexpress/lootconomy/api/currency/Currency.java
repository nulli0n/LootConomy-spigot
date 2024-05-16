package su.nightexpress.lootconomy.api.currency;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.money.object.DeathPenalty;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.Placeholder;
import su.nightexpress.nightcore.util.wrapper.UniParticle;
import su.nightexpress.nightcore.util.wrapper.UniSound;

public interface Currency extends Placeholder {

    @NotNull
    default String formatValue(double amount) {
        return NumberUtil.format(amount);
    }

    @NotNull
    default String format(double amount) {
        return this.applyFormat(this.getFormat(), amount);
    }

    @NotNull
    default String dropFormat(double amount) {
        return this.applyFormat(this.getDropFormat(), amount);
    }

    @NotNull
    default String applyFormat(@NotNull String format, double amount) {
        return this.replacePlaceholders().apply(format).replace(Placeholders.GENERIC_AMOUNT, this.formatValue(amount));
    }

    default double round(double amount) {
        return NumberUtil.round(amount);
    }

    default boolean hasDailyLimit() {
        return this.getDailyLimit() >= 0D;
    }

    boolean isEnabled();

    @NotNull CurrencyHandler getHandler();

    @NotNull String getId();

    @NotNull String getName();

    @NotNull String getFormat();

    @NotNull String getDropFormat();

    @NotNull UniParticle getGroundEffect();

    @NotNull UniSound getPickupSound();

    boolean isDirectToBalance();

    double getDailyLimit();

    @NotNull DeathPenalty getDeathPenalty();

    @NotNull ItemStack getIcon(double amount);
}
