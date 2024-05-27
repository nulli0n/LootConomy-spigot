package su.nightexpress.lootconomy.booster.config;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.nightcore.config.FileConfig;

public class RankBoosterInfo extends BoosterInfo {

    private final String rank;
    private final int priority;

    public RankBoosterInfo(@NotNull String rank, int priority, @NotNull Multiplier multiplier) {
        super(multiplier);
        this.rank = rank.toLowerCase();
        this.priority = priority;
    }

    @NotNull
    public static RankBoosterInfo read(@NotNull FileConfig config, @NotNull String path, @NotNull String rank) {
        Multiplier multiplier = Multiplier.read(config, path);
        int priority = config.getInt(path + ".Priority");

        return new RankBoosterInfo(rank, priority, multiplier);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Priority", this.getPriority());
        super.write(config, path);
    }

    @NotNull
    public String getRank() {
        return rank;
    }

    public int getPriority() {
        return priority;
    }
}
