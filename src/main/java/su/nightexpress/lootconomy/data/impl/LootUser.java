package su.nightexpress.lootconomy.data.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.nightcore.db.AbstractUser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LootUser extends AbstractUser {

    private final Map<String, ExpirableBooster> boosterMap;
    private final UserSettings  settings;

    private LootLimitData limitData;

    @NotNull
    public static LootUser create(@NotNull UUID uuid, @NotNull String name) {
        long dateCreated = System.currentTimeMillis();
        long lastOnline = System.currentTimeMillis();

        Map<String, ExpirableBooster> boosterMap = new HashMap<>();
        UserSettings settings = new UserSettings();
        LootLimitData limitData = LootLimitData.create();

        return new LootUser(uuid, name, dateCreated, lastOnline, limitData, boosterMap, settings);
    }

    public LootUser(@NotNull UUID uuid,
                    @NotNull String name,
                    long lastOnline,
                    long dateCreated,
                    @NotNull LootLimitData limitData,
                    @NotNull Map<String, ExpirableBooster> boosterMap,
                    @NotNull UserSettings settings) {
        super(uuid, name, dateCreated, lastOnline);
        this.setLimitData(limitData);
        this.boosterMap = new ConcurrentHashMap<>(boosterMap);
        this.settings = settings;
    }

    public boolean addBooster(@NotNull String name, @NotNull ExpirableBooster booster) {
        if (booster.isExpired()) return false;

        this.getBoosterMap().put(name.toLowerCase(), booster);
        return true;
    }

    public boolean removeBooster(@NotNull String name) {
        ExpirableBooster booster = this.getBoosterMap().remove(name.toLowerCase());
        return booster != null;
    }

    @NotNull
    public UserSettings getSettings() {
        return settings;
    }

    @NotNull
    public Map<String, ExpirableBooster> getBoosterMap() {
        this.boosterMap.values().removeIf(ExpirableBooster::isExpired);
        return this.boosterMap;
    }

    @NotNull
    public Collection<ExpirableBooster> getBoosters() {
        return this.getBoosterMap().values();
    }

    @Nullable
    public ExpirableBooster getBooster(@NotNull String name) {
        return this.getBoosterMap().get(name.toLowerCase());
    }

    @NotNull
    public LootLimitData getLimitData() {
        if (this.limitData == null || this.limitData.isExpired()) {
            this.limitData = LootLimitData.create();
        }
        return this.limitData;
    }

    public void setLimitData(@Nullable LootLimitData limitData) {
        if (limitData == null || limitData.isExpired()) {
            limitData = LootLimitData.create();
        }
        this.limitData = limitData;
    }
}
