package su.nightexpress.lootconomy.data;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.data.impl.LootLimitData;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.data.impl.UserSettings;
import su.nightexpress.lootconomy.data.serialize.BoosterMultiplierSerializer;
import su.nightexpress.lootconomy.data.serialize.ExpirableBoosterSerializer;
import su.nightexpress.lootconomy.data.serialize.LimitDataSerializer;
import su.nightexpress.lootconomy.data.serialize.UserSettingsSerializer;
import su.nightexpress.nightcore.db.AbstractUserDataManager;
import su.nightexpress.nightcore.db.sql.column.Column;
import su.nightexpress.nightcore.db.sql.column.ColumnType;
import su.nightexpress.nightcore.db.sql.query.impl.SelectQuery;
import su.nightexpress.nightcore.db.sql.query.type.ValuedQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class DataHandler extends AbstractUserDataManager<LootConomyPlugin, LootUser> {

    private static final Column COLUMN_LIMIT_DATA = Column.of("limitdata", ColumnType.STRING);
    private static final Column COLUMN_BOOSTERS   = Column.of("boosters", ColumnType.STRING);
    private static final Column COLUMN_SETTINGS   = Column.of("settings", ColumnType.STRING);

    public DataHandler(@NotNull LootConomyPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected Function<ResultSet, LootUser> createUserFunction() {
        return resultSet -> {
            try {
                UUID uuid = UUID.fromString(resultSet.getString(COLUMN_USER_ID.getName()));
                String name = resultSet.getString(COLUMN_USER_NAME.getName());
                long dateCreated = resultSet.getLong(COLUMN_USER_DATE_CREATED.getName());
                long lastOnline = resultSet.getLong(COLUMN_USER_LAST_ONLINE.getName());

                LootLimitData limitData = this.gson.fromJson(resultSet.getString(COLUMN_LIMIT_DATA.getName()), new TypeToken<LootLimitData>(){}.getType());
                if (limitData == null) limitData = LootLimitData.create();

                Map<String, ExpirableBooster> boosters = this.gson.fromJson(resultSet.getString(COLUMN_BOOSTERS.getName()), new TypeToken<Map<String, ExpirableBooster>>(){}.getType());
                if (boosters == null) boosters = new HashMap<>();

                UserSettings settings = this.gson.fromJson(resultSet.getString(COLUMN_SETTINGS.getName()), new TypeToken<UserSettings>(){}.getType());
                if (settings == null) settings = new UserSettings();

                return new LootUser(uuid, name, dateCreated, lastOnline, limitData, boosters, settings);
            }
            catch (SQLException ex) {
                return null;
            }
        };
    }

    @Override
    protected void addUpsertQueryData(@NotNull ValuedQuery<?, LootUser> query) {
        query.setValue(COLUMN_LIMIT_DATA, user -> this.gson.toJson(user.getLimitData()));
        query.setValue(COLUMN_BOOSTERS, user -> this.gson.toJson(user.getBoosterMap()));
        query.setValue(COLUMN_SETTINGS, user -> this.gson.toJson(user.getSettings()));
    }

    @Override
    protected void addSelectQueryData(@NotNull SelectQuery<LootUser> query) {
        query.column(COLUMN_LIMIT_DATA);
        query.column(COLUMN_SETTINGS);
        query.column(COLUMN_BOOSTERS);
    }

    @Override
    protected void addTableColumns(@NotNull List<Column> columns) {
        columns.add(COLUMN_LIMIT_DATA);
        columns.add(COLUMN_SETTINGS);
        columns.add(COLUMN_BOOSTERS);
    }

    @Override
    public void onSynchronize() {
        this.plugin.getUserManager().getLoaded().forEach(user -> {
            if (user.isAutoSavePlanned()) return;

            LootUser fetched = this.getUser(user.getId());
            if (fetched == null) return;

            if (!user.isAutoSyncReady()) return;

            user.getBoosterMap().clear();
            user.getBoosterMap().putAll(fetched.getBoosterMap());

            user.setLimitData(fetched.getLimitData());
        });
    }

    @Override
    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        return builder
            .registerTypeAdapter(UserSettings.class, new UserSettingsSerializer())
            .registerTypeAdapter(LootLimitData.class, new LimitDataSerializer())
            .registerTypeAdapter(Multiplier.class, new BoosterMultiplierSerializer())
            .registerTypeAdapter(ExpirableBooster.class, new ExpirableBoosterSerializer());
    }
}
