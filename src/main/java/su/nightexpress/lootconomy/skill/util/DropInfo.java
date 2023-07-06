package su.nightexpress.lootconomy.skill.util;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.random.Rnd;

public class DropInfo {

    public static final DropInfo EMPTY = new DropInfo(0, 0, 0);

    private final double chance;
    private final double min;
    private final double max;

    public DropInfo(double chance, double min, double max) {
        this.chance = chance;
        this.min = min;
        this.max = max;
    }

    public static DropInfo read(@NotNull JYML cfg, @NotNull String path) {
        double chance = cfg.getDouble(path + ".Chance");
        double min = cfg.getDouble(path + ".Min");
        double max = cfg.getDouble(path + ".Max");

        return new DropInfo(chance, min, max);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Chance", this.getChance());
        cfg.set(path + ".Min", this.getMin());
        cfg.set(path + ".Max", this.getMax());
    }

    public boolean isEmpty() {
        return this.getChance() <= 0D || (this.getMin() == 0 && this.getMax() == 0);
    }

    public boolean checkChance() {
        return Rnd.chance(this.getChance());
    }

    public double rollAmountNaturally() {
        if (this.isEmpty() || !this.checkChance()) return 0D;

        return this.rollAmount();
    }

    public double rollAmount() {
        return Rnd.getDouble(this.getMin(), this.getMax());
    }

    public double getChance() {
        return chance;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
}
