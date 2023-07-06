package su.nightexpress.lootconomy.booster.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.booster.BoosterMultiplier;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.Set;

public class BoosterInfo {

    private final Set<String>       skills;
    private final BoosterMultiplier multiplier;

    public BoosterInfo(@NotNull Set<String> skills, @NotNull BoosterMultiplier multiplier) {
        this.skills = skills;
        this.multiplier = multiplier;
    }

    @NotNull
    public static BoosterInfo read(@NotNull JYML cfg, @NotNull String path) {
        Set<String> skills = cfg.getStringSet(path + ".Skills");
        BoosterMultiplier multiplier = BoosterMultiplier.read(cfg, path);

        return new BoosterInfo(skills, multiplier);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Skills", this.getSkills());
        this.getMultiplier().write(cfg, path);
    }

    @NotNull
    public Booster createBooster() {
        return new Booster(this);
    }

    public boolean isApplicable(@NotNull Skill skill) {
        return this.isApplicable(skill.getId());
    }

    public boolean isApplicable(@NotNull String skillId) {
        return this.getSkills().contains(skillId) || this.getSkills().contains(Placeholders.WILDCARD);
    }

    @NotNull
    public Set<String> getSkills() {
        return skills;
    }

    @NotNull
    public BoosterMultiplier getMultiplier() {
        return multiplier;
    }
}
