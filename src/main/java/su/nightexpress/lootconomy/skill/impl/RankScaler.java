package su.nightexpress.lootconomy.skill.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.lootconomy.Placeholders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RankScaler extends Scaler {

    public RankScaler(@NotNull Skill job, @NotNull String path) {
        this(job.getConfig(), path, job.getHighestRank());
    }

    public RankScaler(@NotNull JYML cfg, @NotNull String path, @NotNull Rank rank) {
        this(cfg, path, rank.getMaxLevel());
    }

    public RankScaler(@NotNull JYML cfg, @NotNull String path, int maxLevel) {
        super(cfg, path, Placeholders.SKILL_DATA_LEVEL, 1, maxLevel);
    }

    @NotNull
    public static RankScaler read(@NotNull Skill job, @NotNull String path, @NotNull String def, @Nullable String... comments) {
        job.getConfig().addMissing(path, def);
        if (comments != null) {
            List<String> list = new ArrayList<>(Arrays.asList(comments));
            list.add("You can use simple math expressions here: " + Placeholders.URL_ENGINE_SCALER);
            list.add("Level placeholder: " + Placeholders.SKILL_DATA_LEVEL);
            job.getConfig().setComments(path, list);
        }
        return new RankScaler(job, path);
    }
}
