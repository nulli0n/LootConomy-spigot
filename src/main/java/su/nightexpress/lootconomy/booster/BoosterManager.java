package su.nightexpress.lootconomy.booster;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.booster.config.BoosterInfo;
import su.nightexpress.lootconomy.booster.config.RankBoosterInfo;
import su.nightexpress.lootconomy.booster.config.ScheduledBoosterInfo;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.booster.listener.BoosterListener;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BoosterManager extends AbstractManager<LootConomyPlugin> {

    private static final String FILE_NAME = "booster_data";

    private final Map<String, ExpirableBooster> boosterMap;

    public BoosterManager(@NotNull LootConomyPlugin plugin) {
        super(plugin);
        this.boosterMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.loadBoosters();

        this.addListener(new BoosterListener(this.plugin, this));

        this.addTask(this.plugin.createAsyncTask(this::tickScheduledBoosters).setSecondsInterval(Config.BOOSTER_SCHEDULER_INTERVAL.get()));
    }

    @Override
    protected void onShutdown() {
        this.saveBoosters();

        this.boosterMap.clear();
    }

    @NotNull
    public static String formatBoosterValue(double amount) {
        String format = amount >= 0 ? Config.BOOSTER_FORMAT_POSITIVE.get() : Config.BOOSTER_FORMAT_NEGATIVE.get();
        return format.replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(amount));
    }

    private void loadBoosters() {
        FileConfig config = this.getConfig();

        config.getSection("booster").forEach(id -> {
            ExpirableBooster booster = this.loadBooster(config, id);
            if (booster.isExpired()) return;

            this.addBooster(id, booster, true);
        });
    }

    private void saveBoosters() {
        FileConfig config = this.getConfig();

        config.remove("booster");

        this.getBoosterMap().forEach((id, booster) -> {
            this.saveBooster(config, booster, id);
        });

        config.saveChanges();
    }

    @NotNull
    private ExpirableBooster loadBooster(@NotNull FileConfig config, @NotNull String id) {
        long expireDate = config.getLong("booster." + id + ".expire_date");
        Multiplier multiplier = Multiplier.read(config, "booster." + id + ".multiplier");

        return new ExpirableBooster(multiplier, expireDate);
    }

    private void saveBooster(@NotNull FileConfig config, @NotNull ExpirableBooster booster, @NotNull String id) {
        config.set("booster." + id + ".expire_date", booster.getExpireDate());
        booster.getMultiplier().write(config, "booster." + id + ".multiplier");
    }

    @NotNull
    private FileConfig getConfig() {
        return FileConfig.loadOrExtract(this.plugin, FILE_NAME);
    }

    @NotNull
    public Map<String, ScheduledBoosterInfo> getScheduledBoosterMap() {
        return new HashMap<>(Config.BOOSTERS_SCHEDULED.get());
    }

    @NotNull
    public Map<String, RankBoosterInfo> getRankBoosterMap() {
        return new HashMap<>(Config.BOOSTERS_RANK.get());
    }

    @NotNull
    public Map<String, ExpirableBooster> getBoosterMap() {
        this.boosterMap.values().removeIf(ExpirableBooster::isExpired);

        return this.boosterMap;
    }

    @NotNull
    public Set<ExpirableBooster> getBoosters() {
        return new HashSet<>(this.boosterMap.values());
    }

    @Nullable
    public ExpirableBooster getBooster(@NotNull String name) {
        return this.boosterMap.get(name.toLowerCase());
    }

    @Nullable
    public Booster getRankBooster(@NotNull Player player) {
        return this.getRankBoosterMap().values().stream()
            .filter(booster -> Players.getPermissionGroups(player).contains(booster.getRank()))
            .max(Comparator.comparingInt(RankBoosterInfo::getPriority)).map(BoosterInfo::createBooster).orElse(null);
    }

    @NotNull
    public Set<Booster> getBoosters(@NotNull Player player) {
        Set<Booster> boosters = new HashSet<>();

        LootUser user = plugin.getUserManager().getUserData(player);

        // If user has personal booster with the name as global ones, use global one only.
        Set<ExpirableBooster> customBoosters = this.getBoosters();
        user.getBoosterMap().forEach((name, booster) -> {
            ExpirableBooster custom = this.getBooster(name);
            if (custom != null) {
                boosters.add(custom);
                customBoosters.remove(custom);
            }
            else boosters.add(booster);
        });

        boosters.add(this.getRankBooster(player));
        boosters.addAll(customBoosters);
        boosters.removeIf(Objects::isNull);

        return boosters;
    }

    public void tickScheduledBoosters() {
        this.getScheduledBoosterMap().forEach((id, boosterInfo) -> {
            if (!boosterInfo.isReady()) return;

            this.activateBooster(id, boosterInfo);
        });
    }

    public boolean activateBooster(@NotNull String id) {
        ScheduledBoosterInfo boosterInfo = this.getScheduledBoosterMap().get(id.toLowerCase());
        if (boosterInfo == null) return false;

        this.activateBooster(id, boosterInfo);
        return true;
    }

    public void activateBooster(@NotNull String id, @NotNull ScheduledBoosterInfo boosterInfo) {
        ExpirableBooster booster = boosterInfo.createBooster();
        this.addBooster(id, booster, Lang.BOOSTER_NOTIFY_SCHEDULED);
    }

    public boolean addBooster(@NotNull String name, @NotNull ExpirableBooster booster, boolean notify) {
        return this.addBooster(name, booster, notify ? Lang.BOOSTER_NOTIFY_CUSTOM : null);
    }

    private boolean addBooster(@NotNull String name, @NotNull ExpirableBooster booster, @Nullable LangText notify) {
        if (booster.isExpired()) return false;

        this.boosterMap.put(name.toLowerCase(), booster);

        if (notify != null) {
            this.notifyBooster(booster, notify);
        }

        return true;
    }

    public boolean removeBooster(@NotNull String name) {
        ExpirableBooster booster = this.boosterMap.remove(name.toLowerCase());
        return booster != null;
    }

    public void notifyBooster(@NotNull ExpirableBooster booster, @NotNull LangText text) {
        text.getMessage()
            .replace(Placeholders.GENERIC_TIME, TimeUtil.formatDuration(booster.getExpireDate()))
            .replace(Placeholders.GENERIC_ENTRY, list -> {
                plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
                    if (!booster.getMultiplier().has(currency)) return;

                    list.add(currency.replacePlaceholders().apply(Lang.BOOSTER_NOTIFY_ENTRY.getString())
                        .replace(Placeholders.GENERIC_AMOUNT, booster.getMultiplier().formattedPercent(currency))
                        .replace(Placeholders.GENERIC_MULTIPLIER, booster.getMultiplier().formattedMultiplier(currency))
                    );
                });
            })
            .broadcast();
    }

    public void printBoosters(@NotNull CommandSender sender) {
        Set<Booster> userBoosters = sender instanceof Player player ? this.getBoosters(player) : new HashSet<>();
        Set<Booster> globalBoosters = new HashSet<>(this.getBoosters());
        userBoosters.removeAll(globalBoosters);

        Lang.BOOSTER_LIST_INFO.getMessage()
            .replace(Placeholders.GENERIC_GLOBAL, list -> this.addPrintInfo(list, globalBoosters, Lang.BOOSTER_LIST_GLOBAL_NOTHING))
            .replace(Placeholders.GENERIC_PERSONAL, list -> this.addPrintInfo(list, userBoosters, Lang.BOOSTER_LIST_PERSONAL_NOTHING))
            .send(sender);
    }

    private void addPrintInfo(@NotNull List<String> list, @NotNull Collection<Booster> boosters, @NotNull LangString fallback) {
        if (boosters.isEmpty()) {
            list.add(fallback.getString());
            return;
        }

        this.plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
            double percent = Booster.getPercent(currency, boosters);
            double modifier = Booster.getMultiplier(currency, boosters);
            if (percent == 0D) return;

            String subBoosters = boosters.stream().map(booster -> {
                String duration = Lang.OTHER_INFINITY.getString();
                if (booster instanceof ExpirableBooster expirableBooster) {
                    duration = TimeUtil.formatDuration(expirableBooster.getExpireDate());
                }

                return Lang.BOOSTER_LIST_ENTRY_HOVER.getString()
                    .replace(Placeholders.GENERIC_AMOUNT, booster.getMultiplier().formattedPercent(currency))
                    .replace(Placeholders.GENERIC_TIME, duration);

            }).collect(Collectors.joining(Placeholders.TAG_LINE_BREAK));

            list.add(currency.replacePlaceholders().apply(Lang.BOOSTER_LIST_ENTRY_CURRENCY.getString())
                .replace(Placeholders.GENERIC_TOTAL, formatBoosterValue(percent))
                .replace(Placeholders.GENERIC_MULTIPLIER, NumberUtil.format(modifier))
                .replace(Placeholders.GENERIC_ENTRY, subBoosters)
            );
        });
    }
}
