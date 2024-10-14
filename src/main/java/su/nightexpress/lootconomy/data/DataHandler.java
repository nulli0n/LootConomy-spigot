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
import su.nightexpress.nightcore.database.AbstractUserDataHandler;
import su.nightexpress.nightcore.database.sql.SQLColumn;
import su.nightexpress.nightcore.database.sql.SQLValue;
import su.nightexpress.nightcore.database.sql.column.ColumnType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class DataHandler extends AbstractUserDataHandler<LootConomyPlugin, LootUser> {

    private final  Function<ResultSet, LootUser> userFunction;

    private static final SQLColumn COLUMN_LIMIT_DATA = SQLColumn.of("limitdata", ColumnType.STRING);
    private static final SQLColumn COLUMN_BOOSTERS   = SQLColumn.of("boosters", ColumnType.STRING);
    private static final SQLColumn COLUMN_SETTINGS   = SQLColumn.of("settings", ColumnType.STRING);

    public DataHandler(@NotNull LootConomyPlugin plugin) {
        super(plugin);

        this.userFunction = resultSet -> {
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

                return new LootUser(plugin, uuid, name, dateCreated, lastOnline, limitData, boosters, settings);
            }
            catch (SQLException ex) {
                return null;
            }
        };
    }

    @Override
    public void onSynchronize() {
        this.plugin.getUserManager().getLoaded().forEach(user -> {
            if (plugin.getUserManager().isScheduledToSave(user)) return;

            LootUser fetched = this.getUser(user.getId());
            if (fetched == null) return;

            if (!user.isSyncReady()) return;

            user.getBoosterMap().clear();
            user.getBoosterMap().putAll(fetched.getBoosterMap());

            user.setLimitData(fetched.getLimitData());
        });
    }

    @Override
    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        return super.registerAdapters(builder
            .registerTypeAdapter(UserSettings.class, new UserSettingsSerializer())
            .registerTypeAdapter(LootLimitData.class, new LimitDataSerializer())
            .registerTypeAdapter(Multiplier.class, new BoosterMultiplierSerializer())
            .registerTypeAdapter(ExpirableBooster.class, new ExpirableBoosterSerializer())
        );
    }

    @Override
    @NotNull
    protected List<SQLColumn> getExtraColumns() {
        return Arrays.asList(COLUMN_LIMIT_DATA, COLUMN_SETTINGS, COLUMN_BOOSTERS);
    }

    @Override
    @NotNull
    protected List<SQLValue> getSaveColumns(@NotNull LootUser user) {
        return Arrays.asList(
            COLUMN_LIMIT_DATA.toValue(this.gson.toJson(user.getLimitData())),
            COLUMN_BOOSTERS.toValue(this.gson.toJson(user.getBoosterMap())),
            COLUMN_SETTINGS.toValue(this.gson.toJson(user.getSettings()))
        );
    }

    @Override
    @NotNull
    protected Function<ResultSet, LootUser> getUserFunction() {
        return this.userFunction;
    }
}
