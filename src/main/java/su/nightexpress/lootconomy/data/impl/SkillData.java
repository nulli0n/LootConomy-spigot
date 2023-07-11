package su.nightexpress.lootconomy.data.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.skill.impl.Rank;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SkillData implements Placeholder {

    private final Skill          skill;
    private final SkillLimitData limitData;

    private Rank rank;
    private int level;
    private int xp;

    private final Set<Integer> obtainedLevelRewards;
    private final Map<String, Integer> perkLevels;
    private final PlaceholderMap placeholderMap;

    public static SkillData create(@NotNull Skill job) {
        Rank rank = job.getLowestRank();
        return new SkillData(job, rank, 1, 0, SkillLimitData.create(job), new HashMap<>(), new HashSet<>());
    }

    public SkillData(@NotNull Skill skill, @NotNull Rank rank,
                     int level, int xp,
                     @NotNull SkillLimitData limitData,
                     @NotNull Map<String, Integer> perkLevels,
                     @NotNull Set<Integer> obtainedLevelRewards) {
        this.skill = skill;
        this.rank = rank;
        this.limitData = limitData;
        this.perkLevels = perkLevels;
        this.obtainedLevelRewards = obtainedLevelRewards;
        this.setLevel(level);
        this.setXP(xp);

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.SKILL_DATA_LEVEL, () -> NumberUtil.format(this.getLevel()))
            .add(Placeholders.SKILL_DATA_LEVEL_MAX, () -> NumberUtil.format(this.getMaxLevel()))
            .add(Placeholders.SKILL_DATA_XP, () -> NumberUtil.format(this.getXP()))
            .add(Placeholders.SKILL_DATA_XP_MAX, () -> NumberUtil.format(this.getMaxXP()))
            .add(Placeholders.SKILL_DATA_XP_TO_UP, () -> NumberUtil.format(this.getXPToLevelUp()))
            .add(Placeholders.SKILL_DATA_XP_TO_DOWN, () -> NumberUtil.format(this.getXPToLevelDown()))
            .add(Placeholders.SKILL_DATA_RANK, () -> this.getRank().getName())
            .add(Placeholders.SKILL_DATA_NEXT_RANK, () -> this.hasNextRank() ? this.getNextRank().getName() : "-")
            .add(Placeholders.SKILL_DATA_NEXT_RANK_LEVEL, () -> this.hasNextRank() ? NumberUtil.format(this.getMaxLevel() + 1) : "-")
            .add(Placeholders.SKILL_DATA_PREVIOUS_RANK, () -> this.hasPreviousRank() ? this.getPreviousRank().getName() : "-")
            .add(Placeholders.SKILL_DATA_PREVIOUS_RANK_LEVEL, () -> this.hasPreviousRank() ? NumberUtil.format(this.getPreviousRank().getMaxLevel()) : "-")
        ;
        this.placeholderMap.add(skill.getPlaceholders());
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    public void reset() {
        this.setRank(this.getSkill().getLowestRank());
        this.setLevel(1);
        this.setXP(0);
        this.getPerkLevels().clear();

        this.update();
    }


    public void update() {
        boolean isMaxLevel = this.getLevel() >= this.getMaxLevel();
        if (this.getXP() >= this.getMaxXP() && (!isMaxLevel || this.hasNextRank())) {
            this.upLevel(this.getXP() - this.getMaxXP());
        }

        if (this.getXP() <= -(this.getMaxXP()) && (this.getLevel() > 1 || this.hasPreviousRank())) {
            this.downLevel(this.getXP() - -(this.getMaxXP()));
        }
    }

    public void normalize() {
        if (this.getLevel() > this.getMaxLevel()) {
            if (this.hasNextRank()) {
                this.upLevel(this.getXP());
            }
            else {
                this.setLevel(this.getMaxLevel());
            }
        }
        this.setRank(this.getSkill().getRank(this.getLevel()));

        boolean isMaxLevel = this.getLevel() >= this.getMaxLevel();
        if (this.getXP() >= this.getMaxXP()) {
            if ((!isMaxLevel || this.hasNextRank())) {
                this.upLevel(this.getXP() - this.getMaxXP());
            }
            else this.setXP(this.getMaxXP());
        }

        boolean isFirstLevel = this.getLevel() == 1;
        if (this.getXP() <= -(this.getMaxXP())) {
            if ((!isFirstLevel || this.hasPreviousRank())) {
                this.downLevel(this.getXP() - -(this.getMaxXP()));
            }
            else this.setXP(-this.getMaxXP());
        }
    }

    public boolean isCurrencyLimitReached(@NotNull Currency currency) {
        return this.isCurrencyLimitReached(currency.getId());
    }

    public boolean isCurrencyLimitReached(@NotNull String id) {
        this.getLimitData().checkExpiration();

        double limit = this.getRank().getCurrencyLimit(id, this.getLevel());
        return limit > 0 && this.getLimitData().getCurrencyEarned(id) >= limit;
    }

    public boolean isXPLimitReached() {
        this.getLimitData().checkExpiration();

        double limit = this.getRank().getXPLimit(this.getLevel());
        return limit > 0 && this.getLimitData().getXPEarned() >= limit;
    }

    @NotNull
    public Rank getNextRank() {
        return this.getSkill().getNextRank(this.getRank());
    }

    @NotNull
    public Rank getPreviousRank() {
        return this.getSkill().getPreviousRank(this.getRank());
    }

    public boolean hasNextRank() {
        return this.getRank() != this.getNextRank();
    }

    public boolean hasPreviousRank() {
        return this.getRank() != this.getPreviousRank();
    }

    public void removeExp(int remove) {
        remove = Math.abs(remove);

        int toDown = this.getXPToLevelDown();
        if (remove >= toDown) {
            boolean isFirstLevel = this.getLevel() == 1;

            if (isFirstLevel || !this.hasPreviousRank()) {
                this.setXP(-this.getMaxXP());
            }
            else {
                this.downLevel(remove - toDown);
            }
            return;
        }

        this.setXP(this.getXP() - remove);
    }

    public void addExp(int gain) {
        gain = Math.abs(gain);

        int toUp = this.getXPToLevelUp();
        if (gain >= toUp) {
            boolean isMaxLevel = this.getLevel() >= this.getMaxLevel();

            if (isMaxLevel && !this.hasNextRank()) {
                this.setXP(this.getMaxXP());
            }
            else {
                this.upLevel(gain - toUp);
            }
            return;
        }

        this.setXP(this.getXP() + gain);
    }

    public void upLevel(int expLeft) {
        this.setLevel(this.getLevel() + 1);

        Rank next = this.getNextRank();
        if (this.getLevel() >= this.getRank().getMaxLevel() && next != this.getRank()) {
            this.setRank(next);
        }

        int expReq = this.getMaxXP();
        if (expReq <= 0) expReq = this.getRank().getInitialXP();

        this.setXP(expLeft);

        if (this.getXP() >= expReq) {
            if (this.getLevel() >= this.getMaxLevel() && !this.hasNextRank()) {
                this.addExp(1);
            }
            else {
                this.upLevel(expLeft - expReq);
            }
        }
    }

    public void downLevel(int expLeft) {
        if (this.getLevel() == 1) return;
        this.setLevel(this.getLevel() - 1);

        Rank back = this.getPreviousRank();
        if (this.getLevel() <= back.getMaxLevel() && back != this.getRank()) {
            this.setRank(back);
        }

        int expMax = this.getMaxXP();
        if (expMax <= 0) expMax = this.getRank().getInitialXP();

        this.setXP(-Math.abs(expLeft));

        int expDown = -(expMax);
        if (this.getXP() <= expDown) {
            if (this.getLevel() == 1) {
                this.setXP(-this.getXPToLevelDown());
            }
            else {
                this.downLevel((this.getXP() - expDown));
            }
        }
    }

    @NotNull
    public Skill getSkill() {
        return this.skill;
    }

    @NotNull
    public Rank getRank() {
        return rank;
    }

    public void setRank(@NotNull Rank rank) {
        this.rank = rank;
    }

    public int getLevel() {
        return this.level;
    }


    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getMaxLevel() {
        return this.getRank().getMaxLevel();
    }

    public int getXP() {
        return this.xp;
    }

    public void setXP(int xp) {
        this.xp = xp;
    }

    public int getMaxXP() {
        return this.getRank().getXPToLevel(this.getLevel());
    }

    public int getXPToLevelUp() {
        return this.getMaxXP() - this.getXP();
    }

    public int getXPToLevelDown() {
        return this.getXP() + this.getMaxXP();
    }

    @NotNull
    public SkillLimitData getLimitData() {
        return limitData;
    }

    @NotNull
    public Map<String, Integer> getPerkLevels() {
        return perkLevels;
    }

    public int getPerkLevel(@NotNull String id) {
        return this.perkLevels.getOrDefault(id.toLowerCase(), 0);
    }

    public void setPerkLevel(@NotNull String id, int level) {
        this.perkLevels.put(id.toLowerCase(), level);
    }

    @NotNull
    public Set<Integer> getObtainedLevelRewards() {
        return obtainedLevelRewards;
    }

    public boolean isLevelRewardObtained(int level) {
        return this.getObtainedLevelRewards().contains(level);
    }
}
