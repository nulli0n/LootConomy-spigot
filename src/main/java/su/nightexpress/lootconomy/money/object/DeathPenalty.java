package su.nightexpress.lootconomy.money.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

public class DeathPenalty {

    private final boolean   enabled;
    private final boolean forPvE;
    private final boolean forPvP;
    private final boolean   dropItem;
    private final double    chance;
    private final UniDouble amount;

    public DeathPenalty(boolean enabled, boolean forPvE, boolean forPvP, boolean dropItem, double chance, @NotNull UniDouble amount) {
        this.enabled = enabled;
        this.forPvE = forPvE;
        this.forPvP = forPvP;
        this.dropItem = dropItem;
        this.chance = chance;
        this.amount = amount;
    }

    @NotNull
    public static DeathPenalty read(@NotNull FileConfig config, @NotNull String path) {
        boolean enabled = ConfigValue.create(path + ".Enabled",
            false,
            "Sets whether or not death penalty is enabled for this currency.",
            "When enabled, players will lose certain % of their balance on death."
        ).read(config);

        boolean forPvE = ConfigValue.create(path + ".In_PvE",
            true,
            "Sets whether or not death penalty is applicable for PvE (Player vs Environment) deaths."
        ).read(config);

        boolean forPvP = ConfigValue.create(path + ".In_PvP",
            true,
            "Sets whether or not death penalty is applicable for PvP (Player vs Player) deaths."
        ).read(config);

        boolean dropItem = ConfigValue.create(path + ".Drop_Item",
            true,
            "Sets whether or not a currency item should be dropped on player's death, so player can pickup it."
        ).read(config);

        double chance = ConfigValue.create(path + ".Chance",
            25D,
            "Sets the chance the death penalty will happen on death."
        ).read(config);

        UniDouble amount = ConfigValue.create(path + ".Amount",
            (cfg, path2, def) -> UniDouble.read(cfg, path2),
            (cfg, path2, range) -> range.write(cfg, path2),
            () -> UniDouble.of(1, 3),
            "Sets currency amount (in % of player's balance) to drop on death."
        ).read(config);

        return new DeathPenalty(enabled, forPvE, forPvP, dropItem, chance, amount);
    }

    public double rollAmount() {
        return Rnd.getDouble(Math.abs(this.getMinAmount()), Math.abs(this.getMaxAmount()));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isForPvE() {
        return forPvE;
    }

    public boolean isForPvP() {
        return forPvP;
    }

    public boolean isDropItem() {
        return dropItem;
    }

    public double getChance() {
        return chance;
    }

    public double getMinAmount() {
        return this.amount.getMinValue();
    }

    public double getMaxAmount() {
        return this.amount.getMaxValue();
    }
}
