package su.nightexpress.lootconomy.booster.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.lootconomy.booster.BoosterMultiplier;

import java.util.Set;

public class RankBoosterInfo extends BoosterInfo {

    private final String rank;
    private final int priority;

    public RankBoosterInfo(@NotNull String rank, int priority,
                           @NotNull Set<String> skills, @NotNull BoosterMultiplier multiplier) {
        super(skills, multiplier);
        this.rank = rank.toLowerCase();
        this.priority = priority;
    }

    @NotNull
    public static RankBoosterInfo read(@NotNull JYML cfg, @NotNull String path, @NotNull String rank) {
        Set<String> skills = cfg.getStringSet(path + ".Skills");
        BoosterMultiplier multiplier = BoosterMultiplier.read(cfg, path);
        int priority = cfg.getInt(path + ".Priority");

        return new RankBoosterInfo(rank, priority, skills, multiplier);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Priority", this.getPriority());
        super.write(cfg, path);
    }

    @NotNull
    public String getRank() {
        return rank;
    }

    public int getPriority() {
        return priority;
    }
}
