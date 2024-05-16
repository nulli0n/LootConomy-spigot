package su.nightexpress.lootconomy.money.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniDouble;
import su.nightexpress.nightcore.util.wrapper.UniInt;

public class DropInfo {

    public static final DropInfo EMPTY = new DropInfo(0, UniDouble.of(0, 0), UniInt.of(1, 1), "null");

    private final double    chance;
    private final UniDouble amount;
    private final UniInt    portions;
    private final String customMultiplier;

    public DropInfo(double chance, @NotNull UniDouble amount, @NotNull UniInt portions, @NotNull String customMultiplier) {
        this.chance = chance;
        this.amount = amount;
        this.portions = portions;
        this.customMultiplier = customMultiplier;
    }

    @NotNull
    public static DropInfo read(@NotNull FileConfig config, @NotNull String path) {
        double chance = config.getDouble(path + ".Chance");
        UniDouble amount = UniDouble.read(config, path + ".Amount");
        UniInt portions = UniInt.read(config, path + ".Portions");
        String customMultiplier = ConfigValue.create(path + ".Custom_Multiplier","null").read(config);

        return new DropInfo(chance, amount, portions, customMultiplier);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Chance", this.getChance());
        this.amount.write(config, path + ".Amount");
        this.portions.write(config, path + ".Portions");
        config.set(path + ".Custom_Multiplier", this.customMultiplier);
    }

    public boolean isEmpty() {
        return this.getChance() <= 0D || this.getMaxAmount() == 0D;
    }

    public boolean isPenalty() {
        return this.getMinAmount() < 0D || this.getMaxAmount() < 0D;
    }

    public boolean checkChance() {
        return Rnd.chance(this.getChance());
    }

    public double rollAmountNaturally() {
        if (this.isEmpty() || !this.checkChance()) return 0D;

        double rolled = this.rollAmount();

        if (this.isPenalty()) {
            rolled = -(rolled);
        }

        return rolled;
    }

    public double rollAmount() {
        return Rnd.getDouble(Math.abs(this.getMinAmount()), Math.abs(this.getMaxAmount()));
    }

    public int rollPortions() {
        int min = Math.max(1, Math.abs(this.portions.getMinValue()));
        int max = Math.max(1, Math.abs(this.portions.getMaxValue()));

        return Rnd.get(min, max);
    }

    public double getMinAmount() {
        return this.getAmount().getMinValue();
    }

    public double getMaxAmount() {
        return this.getAmount().getMaxValue();
    }

    public double getChance() {
        return chance;
    }

    @NotNull
    public UniDouble getAmount() {
        return amount;
    }

    @NotNull
    public UniInt getPortions() {
        return portions;
    }

    @NotNull
    public String getCustomMultiplier() {
        return customMultiplier;
    }
}
