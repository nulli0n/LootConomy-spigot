package su.nightexpress.lootconomy.booster;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.booster.config.BoosterInfo;
import su.nightexpress.lootconomy.booster.config.RankBoosterInfo;
import su.nightexpress.lootconomy.booster.config.TimedBoosterInfo;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.booster.listener.BoosterListenerGeneric;
import su.nightexpress.lootconomy.booster.task.BoosterUpdateTask;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoosterManager extends AbstractManager<LootConomy> {

    private final Map<String, ExpirableBooster> globalBoosterMap;

    private BoosterUpdateTask boosterUpdateTask;

    public BoosterManager(@NotNull LootConomy plugin) {
        super(plugin);
        this.globalBoosterMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.boosterUpdateTask = new BoosterUpdateTask(this.plugin);
        this.boosterUpdateTask.start();

        this.addListener(new BoosterListenerGeneric(this.plugin));
    }

    @Override
    protected void onShutdown() {
        if (this.boosterUpdateTask != null) {
            this.boosterUpdateTask.stop();
            this.boosterUpdateTask = null;
        }
        this.getGlobalBoosterMap().clear();
    }

    @NotNull
    public Map<String, ExpirableBooster> getGlobalBoosterMap() {
        this.globalBoosterMap.values().removeIf(ExpirableBooster::isExpired);
        return globalBoosterMap;
    }

    @Nullable
    public ExpirableBooster getGlobalBooster(@NotNull Skill skill) {
        return this.getGlobalBoosterMap().get(skill.getId());
    }

    @Nullable
    public Booster getRankBooster(@NotNull Player player) {
        return Config.BOOSTERS_RANK.get().values().stream()
            .filter(booster -> VaultHook.getPermissionGroups(player).contains(booster.getRank()))
            .max(Comparator.comparingInt(RankBoosterInfo::getPriority)).map(BoosterInfo::createBooster).orElse(null);
    }

    @NotNull
    public Collection<Booster> getBoosters(@NotNull Player player, @NotNull Skill skill) {
        Set<Booster> boosters = new HashSet<>();

        LootUser user = plugin.getUserManager().getUserData(player);
        boosters.add(user.getBooster(skill));
        boosters.add(this.getGlobalBooster(skill));
        boosters.add(this.getRankBooster(player));
        boosters.removeIf(Objects::isNull);

        return boosters;
    }

    public void updateGlobal() {
        TimedBoosterInfo boosterInfo = Config.BOOSTERS_GLOBAL.get().values().stream()
            .filter(TimedBoosterInfo::isReady).findFirst().orElse(null);

        if (boosterInfo == null) return;

        boosterInfo.getSkills().forEach(skill -> {
            this.getGlobalBoosterMap().put(skill, boosterInfo.createBooster());
        });
    }

    public void notifyBooster() {
        this.notifyBooster(plugin.getServer().getOnlinePlayers().toArray(new Player[0]));
    }

    public void notifyBooster(@NotNull Player... players) {
        // TODOs
        /*List<IBooster> boosters = new ArrayList<>();
        if (boosters.isEmpty()) return;

        List<String> message = plugin.getMessage(Lang.BOOSTER_GLOBAL_NOTIFY).asList();

        for (Player player : players) {
            for (String line : message) {
                if (line.contains("%booster_")) {
                    boosters.forEach(booster -> {
                        MessageUtil.sendWithJSON(player, booster.replacePlaceholders().apply(line));
                    });
                }
                else MessageUtil.sendWithJSON(player, line);
            }
        }*/
    }
}
