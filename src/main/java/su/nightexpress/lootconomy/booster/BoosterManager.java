package su.nightexpress.lootconomy.booster;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.booster.impl.BoosterSchedule;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.booster.listener.BoosterListener;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.placeholder.Replacer;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.util.HashSet;
import java.util.Set;

public class BoosterManager extends AbstractManager<LootConomyPlugin> {

    // TODO Dedicated config, and fully optional boosters module

    private Booster globalBooster;

    public BoosterManager(@NotNull LootConomyPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.addListener(new BoosterListener(this.plugin, this));

        this.addAsyncTask(this::tickBoosters, Config.BOOSTER_TICK_INTERVAL.get());
    }

    @Override
    protected void onShutdown() {
        this.globalBooster = null;
    }

    public void tickBoosters() {
        this.tickGlobal();
        this.tickSchedules();
        this.tickPersonal();
    }

    private void tickGlobal() {
        if (this.globalBooster == null) return;
        if (this.globalBooster.isExpired()) {
            Lang.BOOSTER_EXPIRED_GLOBAL.getMessage().broadcast(replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, this.globalBooster.formattedPercent()));
            this.globalBooster = null;
        }
    }

    private void tickSchedules() {
        if (this.hasGlobalBoost()) return;

        BoosterSchedule ready = this.getBoosterSchedules().stream().filter(BoosterSchedule::isReady).findFirst().orElse(null);
        if (ready == null) return;

        this.activateBooster(ready, true);
    }

    private void tickPersonal() {
        Players.getOnline().forEach(player -> {
            LootUser user = plugin.getUserManager().getOrFetch(player);
            Booster booster = user.getBooster();
            if (booster == null) return;

            if (booster.isExpired()) {
                Lang.BOOSTER_EXPIRED_PERSONAL.getMessage().send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, booster.formattedPercent()));
                user.removeBooster();
            }
        });
    }

    public boolean hasGlobalBoost() {
        return this.globalBooster != null && this.globalBooster.isValid();
    }

    @NotNull
    public Set<BoosterSchedule> getBoosterSchedules() {
        return new HashSet<>(Config.getBoosterScheduleMap().values());
    }

    @Nullable
    public BoosterSchedule getBoosterScheduleById(@NotNull String id) {
        return Config.getBoosterScheduleMap().get(id.toLowerCase());
    }

    @Nullable
    public Booster getGlobalBooster() {
        return this.globalBooster;
    }

    public double getRankBoost(@NotNull Player player) {
        return Config.BOOSTERS_BY_RANK.get().getGreatest(player);
    }

    public double getGlobalBoost() {
        return this.getBoost(this.globalBooster);
    }

    public double getPersonalBoost(@NotNull Player player) {
        LootUser user = plugin.getUserManager().getOrFetch(player);
        return this.getBoost(user.getBooster());
    }

    public double getTotalBoostPercent(@NotNull Player player) {
        double percent = 0D;
        for (BoosterType type : BoosterType.values()) {
            percent += BoosterUtils.getAsPercent(this.getBoosterMultiplier(player, type));
        }
        return percent;
    }

    private double getBoost(@Nullable Booster booster) {
        return booster == null || !booster.isValid() ? 1D : booster.getMultiplier();
    }

    public double getTotalBoost(@NotNull Player player) {
        return this.getTotalBoostPercent(player) / 100D;
    }

    public double getBoosterMultiplier(@NotNull Player player, @NotNull BoosterType type) {
        return switch (type) {
            case RANK -> this.getRankBoost(player);
            case GLOBAL -> this.getGlobalBoost();
            case PERSONAL -> this.getPersonalBoost(player);
        };
    }

    public long getBoosterExpireDate(@NotNull Player player, @NotNull BoosterType type) {
        return switch (type) {
            case PERSONAL -> {
                LootUser user = plugin.getUserManager().getOrFetch(player);
                Booster booster = user.getBooster();
                yield booster == null ? 0L : booster.getExpireDate();
            }
            case GLOBAL -> this.hasGlobalBoost() ? this.globalBooster.getExpireDate() : 0L;
            case RANK -> -1L;
        };
    }

    public boolean hasBoosterMultiplier(@NotNull Player player, @NotNull BoosterType type) {
        return this.getBoosterMultiplier(player, type) != 1D;
    }

    public boolean activateBoosterById(@NotNull String id) {
        BoosterSchedule schedule = this.getBoosterScheduleById(id);
        if (schedule == null) return false;

        this.activateBooster(schedule, false);
        return true;
    }

    public void activateBooster(@NotNull BoosterSchedule schedule, boolean relative) {
        Booster booster = schedule.createBooster(true);
        if (!booster.isValid()) return;

        this.setGlobalBooster(booster);
    }

    public boolean setGlobalBooster(@NotNull Booster booster) {
        this.globalBooster = booster;
        this.notifyGlobalBooster(booster);
        return true;
    }

    public void removeGlobalBooster() {
        this.globalBooster = null;
    }

    public void notifyGlobalBooster(@NotNull Booster booster) {
        Lang.BOOSTER_ACTIVATED_GLOBAL.getMessage().broadcast(replacer -> replacer
            .replace(Placeholders.GENERIC_TIME, TimeFormats.formatDuration(booster.getExpireDate(), TimeFormatType.LITERAL))
            .replace(Placeholders.GENERIC_AMOUNT, booster.formattedPercent())
        );
    }

    public void notifyPersonalBooster(@NotNull Player player, @NotNull Booster booster) {
        Lang.BOOSTER_ACTIVATED_PERSONAL.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_TIME, TimeFormats.formatDuration(booster.getExpireDate(), TimeFormatType.LITERAL))
            .replace(Placeholders.GENERIC_AMOUNT, booster.formattedPercent())
        );
    }

    public void displayBoosterInfo(@NotNull Player player) {
        double totalPercent = this.getTotalBoostPercent(player);
        if (totalPercent == 0D) {
            Lang.BOOSTER_LIST_NOTHING.getMessage().send(player);
            return;
        }

        Lang.BOOSTER_LIST_INFO.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_TOTAL, BoosterUtils.formatPercent(totalPercent))
            .replace(Placeholders.GENERIC_ENTRY, list -> {
                for (BoosterType type : BoosterType.values()) {
                    if (!this.hasBoosterMultiplier(player, type)) continue;

                    list.add(Replacer.create()
                        .replace(Placeholders.GENERIC_TYPE, () -> Lang.BOOSTER_TYPE.getLocalized(type))
                        .replace(Placeholders.GENERIC_AMOUNT, () -> BoosterUtils.formatMultiplier(this.getBoosterMultiplier(player, type)))
                        .replace(Placeholders.GENERIC_TIME, () -> {
                            long expireDate = this.getBoosterExpireDate(player, type);
                            return expireDate < 0L ? Lang.OTHER_INFINITY.getString() : TimeFormats.formatDuration(expireDate, TimeFormatType.LITERAL);
                        })
                        .apply(Lang.BOOSTER_LIST_ENTRY.getString())
                    );
                }
            })
        );
    }
}
