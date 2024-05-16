package su.nightexpress.lootconomy.booster.config;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.nightcore.config.FileConfig;

public class BoosterInfo {

    private final Multiplier multiplier;

    public BoosterInfo(@NotNull Multiplier multiplier) {
        this.multiplier = multiplier;
    }

    @NotNull
    public static BoosterInfo read(@NotNull FileConfig config, @NotNull String path) {
        Multiplier multiplier = Multiplier.read(config, path + ".Multiplier");

        return new BoosterInfo(multiplier);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        this.getMultiplier().write(config, path);
    }

    @NotNull
    public Booster createBooster() {
        return new Booster(this);
    }

    @NotNull
    public Multiplier getMultiplier() {
        return multiplier;
    }
}
