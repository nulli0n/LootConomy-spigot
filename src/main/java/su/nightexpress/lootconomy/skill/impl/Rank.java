package su.nightexpress.lootconomy.skill.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;

import java.util.*;

public class Rank implements Placeholder {

    private final String                     id;
    private final String                     name;
    private final int                        maxLevel;
    private final int                        initialXP;
    private final TreeMap<Integer, Integer>  xpTable;
    private final Map<Integer, List<String>> levelCommands;

    private final Map<String, RankScaler> currencyMultiplier;
    private final RankScaler              xpMultiplier;
    private final Map<String, RankScaler> currencyDropLimits;
    private final RankScaler              xpDropLimits;

    private final PlaceholderMap placeholderMap;

    public Rank(
        @NotNull String id,
        @NotNull String name,
        int maxLevel,
        int initialXP,
        @NotNull TreeMap<Integer, Integer> xpTable,
        @NotNull Map<Integer, List<String>> levelCommands,
        @NotNull Map<String, RankScaler> currencyMultiplier,
        @NotNull RankScaler xpMultiplier,
        @NotNull Map<String, RankScaler> currencyDropLimits,
        @NotNull RankScaler xpDropLimits
        ) {
        this.id = id.toLowerCase();
        this.name = Colorizer.apply(name);
        this.maxLevel = Math.max(1, maxLevel);
        this.initialXP = Math.max(1, initialXP);
        this.xpTable = xpTable;
        this.levelCommands = levelCommands;
        this.currencyMultiplier = currencyMultiplier;
        this.xpMultiplier = xpMultiplier;
        this.currencyDropLimits = currencyDropLimits;
        this.xpDropLimits = xpDropLimits;

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.RANK_ID, this::getId)
            .add(Placeholders.RANK_NAME, this::getName)
            .add(Placeholders.RANK_MAX_LEVEL, () -> NumberUtil.format(this.getMaxLevel()))
        ;
    }

    @NotNull
    public static Rank read(@NotNull JYML cfg, @NotNull String path, @NotNull String id) {
        String name = JOption.create(path + ".Name", StringUtil.capitalizeUnderscored(id)).read(cfg);
        int maxLevel = JOption.create(path + ".Level_Cap", 1).read(cfg);
        int xpInitial = JOption.create(path + ".XP_Initial", 25).read(cfg);
        double xpFactor = JOption.create(path + ".XP_Factor", 1D).read(cfg);

        var xpTable = new TreeMap<Integer, Integer>();

        for (int level = 1; level < (maxLevel + 1); level++) {
            int xpPrevious = xpTable.getOrDefault(level - 1, xpInitial);
            int xpToLevel = (int) (xpPrevious * xpFactor);
            xpTable.put(level, xpToLevel);
        }

        Map<Integer, List<String>> levelCommands = JOption.forMap(path + ".LevelUp_Commands",
            (key) -> StringUtil.getInteger(key, 0),
            (cfg2, path2, key) -> cfg2.getStringList(path2 + "." + key),
            Map.of(
                0, Arrays.asList("eco give " + Placeholders.PLAYER_NAME + " 250", "feed " + Placeholders.PLAYER_NAME)
            )
        ).setWriter((cfg2, path2, map) -> map.forEach((lvl, cmds) -> cfg2.set(path2 + "." + lvl, cmds))).read(cfg);

        Map<String, RankScaler> currencyMultiplier = new HashMap<>();
        Map<String, RankScaler> currencyDropLimits = new HashMap<>();
        for (String curId : cfg.getSection(path + ".Drop_Multiplier.Currency")) {
            RankScaler scaler = new RankScaler(cfg, path + ".Drop_Multiplier.Currency." + curId, maxLevel);
            currencyMultiplier.put(curId.toLowerCase(), scaler);
        }
        for (String curId : cfg.getSection(path + ".Daily_Limits.Currency")) {
            RankScaler scaler = new RankScaler(cfg, path + ".Daily_Limits.Currency." + curId, maxLevel);
            currencyDropLimits.put(curId.toLowerCase(), scaler);
        }
        RankScaler xpMultiplier = new RankScaler(cfg, path + ".Drop_Multiplier.XP", maxLevel);
        RankScaler xpDropLimit = new RankScaler(cfg, path + ".Daily_Limits.XP", maxLevel);

        return new Rank(
            id, name,
            maxLevel, xpInitial, xpTable, levelCommands,
            currencyMultiplier, xpMultiplier, currencyDropLimits, xpDropLimit);
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    public double getCurrencyMultiplier(@NotNull Currency currency, int level) {
        return this.getCurrencyMultiplier(currency.getId(), level);
    }

    public double getCurrencyMultiplier(@NotNull String id, int level) {
        RankScaler scaler = this.getCurrencyMultiplier().get(id.toLowerCase());
        return scaler == null ? 1D : scaler.getValue(level);
    }

    public double getCurrencyLimit(@NotNull Currency currency, int level) {
        return this.getCurrencyLimit(currency.getId(), level);
    }

    public double getCurrencyLimit(@NotNull String id, int level) {
        RankScaler scaler = this.getCurrencyDropLimits().get(id.toLowerCase());
        return scaler == null ? -1D : scaler.getValue(level);
    }

    public int getXPToLevel(int level) {
        Map.Entry<Integer, Integer> entry = this.getXPTable().floorEntry(level);
        return entry != null ? entry.getValue() : this.getInitialXP();
    }

    public double getXPMultiplier(int level) {
        return this.getXPMultiplier().getValue(level);
    }

    public double getXPLimit(int level) {
        return this.getXPDropLimits().getValue(level);
    }

    public boolean isCurrencyLimited(@NotNull Currency currency, int level) {
        return this.isCurrencyLimited(currency.getId(), level);
    }

    public boolean isCurrencyLimited(@NotNull String id, int level) {
        return this.getCurrencyLimit(id, level) > 0D;
    }

    public boolean isXPLimited(int level) {
        return this.getXPLimit(level) > 0D;
    }

    @NotNull
    public List<String> getLevelUpCommands(int level) {
        return this.getLevelCommands().getOrDefault(level, Collections.emptyList());
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getInitialXP() {
        return initialXP;
    }

    @NotNull
    public TreeMap<Integer, Integer> getXPTable() {
        return xpTable;
    }

    @NotNull
    public Map<Integer, List<String>> getLevelCommands() {
        return levelCommands;
    }

    @NotNull
    public Map<String, RankScaler> getCurrencyMultiplier() {
        return currencyMultiplier;
    }

    @NotNull
    public Map<String, RankScaler> getCurrencyDropLimits() {
        return currencyDropLimits;
    }

    @NotNull
    public RankScaler getXPMultiplier() {
        return xpMultiplier;
    }

    @NotNull
    public RankScaler getXPDropLimits() {
        return xpDropLimits;
    }
}
