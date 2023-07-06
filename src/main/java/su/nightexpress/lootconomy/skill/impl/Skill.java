package su.nightexpress.lootconomy.skill.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractConfigHolder;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.skill.menu.SkillObjectivesMenu;

import java.util.*;

public class Skill extends AbstractConfigHolder<LootConomy> implements Placeholder, ICleanable {

    private SkillType type;
    private String    name;
    private List<String> description;
    private boolean permissionRequired;
    private ItemStack           icon;
    private SkillObjectivesMenu objectivesMenu;

    private final Map<String, Rank>           rankMap;
    private final Map<String, SkillObjective> objectiveMap;
    private final PlaceholderMap              placeholderMap;

    public Skill(@NotNull LootConomy plugin, @NotNull JYML cfg, @NotNull String id) {
        super(plugin, cfg, id);
        this.rankMap = new HashMap<>();
        this.objectiveMap = new HashMap<>();
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.SKILL_ID, this::getId)
            .add(Placeholders.SKILL_NAME, this::getName)
            .add(Placeholders.SKILL_DESCRIPTION, () -> String.join("\n", this.getDescription()))
            .add(Placeholders.SKILL_PERMISSION_REQUIRED, () -> LangManager.getBoolean(this.isPermissionRequired()))
            .add(Placeholders.SKILL_PERMISSION_NODE, this::getPermission)
        ;
    }

    @Override
    public boolean load() {
        if (!this.cfg.getBoolean("Enabled")) return false;

        this.type = cfg.getEnum("Type", SkillType.class);
        if (this.type == null) {
            this.plugin.error("Invalid type for the '" + this.getId() + "' job!");
            return false;
        }

        this.name = Colorizer.apply(cfg.getString("Name", StringUtil.capitalizeUnderscored(this.getId())));
        this.description = Colorizer.apply(cfg.getStringList("Description"));
        this.permissionRequired = cfg.getBoolean("Permission_Required");
        this.icon = cfg.getItem("Icon");

        for (String rankId : cfg.getSection("Ranks")) {
            Rank rank = Rank.read(this.cfg, "Ranks." + rankId, rankId);
            this.rankMap.put(rank.getId(), rank);
        }

        if (this.getRankMap().isEmpty()) {
            this.plugin.error("No ranks for the '" + this.getId() + "' job available (Code 1)!");
            return false;
        }
        try {
            this.getLowestRank();
            this.getHighestRank();
        }
        catch (NoSuchElementException e) {
            this.plugin.error("No ranks for the '" + this.getId() + "' job available (Code 2)!");
            return false;
        }

        for (JYML objConfig : JYML.loadAll(this.getConfig().getFile().getParentFile().getAbsolutePath() + "/objectives/")) {
            for (String object : objConfig.getSection("")) {
                SkillObjective objective = SkillObjective.read(objConfig, object, object);
                if (objective == null) continue;

                this.objectiveMap.put(objective.getName(), objective);
            }
        }

        this.objectivesMenu = new SkillObjectivesMenu(this.plugin, this);

        return true;
    }

    @Override
    public void onSave() {

    }

    @Override
    public void clear() {
        if (this.objectivesMenu != null) {
            this.objectivesMenu.clear();
        }
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    public SkillObjectivesMenu getObjectivesMenu() {
        return objectivesMenu;
    }

    public boolean hasObjective(@NotNull String name) {
        return this.getObjective(name) != null;
    }

    @Nullable
    public SkillObjective getObjective(@NotNull String name) {
        return this.getObjectiveMap().get(name.toLowerCase());
    }

    @NotNull
    public Rank getLowestRank() {
        return this.getRankMap().values().stream().min(Comparator.comparing(Rank::getMaxLevel)).orElseThrow();
    }

    @NotNull
    public Rank getHighestRank() {
        return this.getRankMap().values().stream().max(Comparator.comparing(Rank::getMaxLevel)).orElseThrow();
    }

    @NotNull
    public Rank getNextRank(@NotNull Rank rank) {
        return this.getNextRank(rank.getMaxLevel() + 1);
    }

    @NotNull
    public Rank getNextRank(int level) {
        return this.getRankMap().values().stream()
            .filter(rank -> rank.getMaxLevel() >= level)
            .min(Comparator.comparing(Rank::getMaxLevel)).orElse(this.getHighestRank());
    }

    @NotNull
    public Rank getPreviousRank(@NotNull Rank rank) {
        return this.getPreviousRank(rank.getMaxLevel() - 1);
    }

    @NotNull
    public Rank getPreviousRank(int level) {
        return this.getRankMap().values().stream()
            .filter(rank -> rank.getMaxLevel() <= level)
            .max(Comparator.comparing(Rank::getMaxLevel)).orElse(this.getLowestRank());
    }

    @NotNull
    public Rank getRank(int level) {
        return this.getRankMap().values().stream()
            .filter(rank -> rank.getMaxLevel() >= level)
            .min(Comparator.comparing(Rank::getMaxLevel)).orElse(this.getLowestRank());
    }

    @Nullable
    public Rank getRank(@NotNull String id) {
        return this.getRankMap().get(id.toLowerCase());
    }

    @NotNull
    public String getPermission() {
        return Perms.PREFIX_SKILL + this.getId();
    }

    public boolean hasPermission(@NotNull Player player) {
        return player.hasPermission(this.getPermission());
    }

    @NotNull
    public SkillType getType() {
        return type;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getDescription() {
        return description;
    }

    public boolean isPermissionRequired() {
        return permissionRequired;
    }

    @NotNull
    public ItemStack getIcon() {
        return icon;
    }

    @NotNull
    public Map<String, Rank> getRankMap() {
        return rankMap;
    }

    @NotNull
    public Map<String, SkillObjective> getObjectiveMap() {
        return objectiveMap;
    }
}
