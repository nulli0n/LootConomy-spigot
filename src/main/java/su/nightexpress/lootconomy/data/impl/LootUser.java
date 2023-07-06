package su.nightexpress.lootconomy.data.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.data.AbstractUser;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LootUser extends AbstractUser<LootConomy> {

    private final Map<String, SkillData> dataMap;
    private final Map<String, ExpirableBooster> boosterMap;
    private final UserSettings      settings;

    public LootUser(@NotNull LootConomy plugin, @NotNull UUID uuid, @NotNull String name) {
        this(plugin, uuid, name, System.currentTimeMillis(), System.currentTimeMillis(),
            new HashMap<>(), // Skill Data
            new HashMap<>(), // Personal Boosters
            new UserSettings()
        );
    }

    public LootUser(
        @NotNull LootConomy plugin,
        @NotNull UUID uuid,
        @NotNull String name,
        long lastOnline,
        long dateCreated,
        @NotNull Map<String, SkillData> dataMap,
        @NotNull Map<String, ExpirableBooster> boosterMap,
        @NotNull UserSettings settings
    ) {
        super(plugin, uuid, name, dateCreated, lastOnline);
        this.dataMap = dataMap;
        this.boosterMap = new ConcurrentHashMap<>(boosterMap);
        this.settings = settings;

        // Update missing skills.
        this.plugin.getSkillManager().getSkills().forEach(this::getData);
    }

    @NotNull
    public UserSettings getSettings() {
        return settings;
    }

    @NotNull
    public Map<String, SkillData> getDataMap() {
        return dataMap;
    }

    @NotNull
    public SkillData getData(@NotNull Skill skill) {
        return this.getDataMap().computeIfAbsent(skill.getId(), k -> SkillData.create(skill));
    }

    @NotNull
    public Collection<SkillData> getDatas() {
        return this.getDataMap().values();
    }

    @NotNull
    public Map<String, ExpirableBooster> getBoosterMap() {
        this.boosterMap.values().removeIf(ExpirableBooster::isExpired);
        return this.boosterMap;
    }

    @NotNull
    @Deprecated
    public Collection<ExpirableBooster> getBoosters() {
        return this.getBoosterMap().values();
    }

    @Nullable
    public ExpirableBooster getBooster(@NotNull Skill skill) {
        return this.getBooster(skill.getId());
    }

    @Nullable
    public ExpirableBooster getBooster(@NotNull String skillId) {
        return this.getBoosterMap().get(skillId.toLowerCase());
    }
}
