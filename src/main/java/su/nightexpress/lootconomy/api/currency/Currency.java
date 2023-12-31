package su.nightexpress.lootconomy.api.currency;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nexmedia.engine.utils.values.UniSound;
import su.nightexpress.lootconomy.Placeholders;

public interface Currency extends Placeholder {

    @NotNull
    default String formatValue(double amount) {
        return NumberUtil.format(amount);
    }

    @NotNull
    default String format(double amount) {
        return this.replacePlaceholders().apply(this.getFormat()).replace(Placeholders.GENERIC_AMOUNT, this.formatValue(amount));
    }

    default double round(double amount) {
        return amount;
    }

    @NotNull CurrencyHandler getHandler();

    @NotNull String getId();

    @NotNull String getName();

    void setName(@NotNull String name);

    @NotNull String getFormat();

    @NotNull UniParticle getGroundEffect();

    @NotNull UniSound getPickupSound();

    boolean isDirectToBalance();

    boolean isDeathPenaltyEnabled();

    boolean isDeathPenaltyDropItem();

    double getDeathPenaltyChance();

    double getDeathPenaltyAmountMin();

    double getDeathPenaltyAmountMax();

    double getDeathPenaltyAmount();

    @NotNull ItemStack getIcon(double amount);
}
