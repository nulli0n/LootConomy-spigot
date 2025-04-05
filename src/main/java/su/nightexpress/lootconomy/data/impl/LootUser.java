package su.nightexpress.lootconomy.data.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.nightcore.db.AbstractUser;

import java.util.UUID;

public class LootUser extends AbstractUser {

    private final UserSettings  settings;

    private LootLimitData limitData;
    private Booster       booster;

    @NotNull
    public static LootUser create(@NotNull UUID uuid, @NotNull String name) {
        long dateCreated = System.currentTimeMillis();
        long lastOnline = System.currentTimeMillis();

        UserSettings settings = new UserSettings();
        LootLimitData limitData = LootLimitData.create();

        return new LootUser(uuid, name, dateCreated, lastOnline, limitData, null, settings);
    }

    public LootUser(@NotNull UUID uuid,
                    @NotNull String name,
                    long lastOnline,
                    long dateCreated,
                    @NotNull LootLimitData limitData,
                    @Nullable Booster booster,
                    @NotNull UserSettings settings) {
        super(uuid, name, dateCreated, lastOnline);
        this.setLimitData(limitData);
        this.setBooster(booster);
        this.settings = settings;
    }

    public void setBooster(@Nullable Booster booster) {
        if (booster == null || !booster.isValid()) {
            this.removeBooster();
            return;
        }

        this.booster = booster;
    }

    public void removeBooster() {
        this.booster = null;
    }

    public boolean hasBooster() {
        return this.booster != null && this.booster.isValid();
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }

    @Nullable
    public Booster getBooster() {
        return this.booster;
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
