package su.nightexpress.lootconomy.data;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLValue;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nexmedia.engine.api.data.sql.executor.SelectQueryExecutor;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.booster.BoosterMultiplier;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.data.impl.SkillLimitData;
import su.nightexpress.lootconomy.data.impl.UserSettings;
import su.nightexpress.lootconomy.data.serialize.*;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class DataHandler extends AbstractUserDataHandler<LootConomy, LootUser> {

    private static DataHandler                   instance;
    private final  Function<ResultSet, LootUser> userFunction;

    private static final SQLColumn COLUMN_DATA     = SQLColumn.of("data", ColumnType.STRING);
    private static final SQLColumn COLUMN_BOOSTERS = SQLColumn.of("boosters", ColumnType.STRING);
    private static final SQLColumn COLUMN_SETTINGS = SQLColumn.of("settings", ColumnType.STRING);

    protected DataHandler(@NotNull LootConomy plugin) {
        super(plugin, plugin);

        this.userFunction = resultSet -> {
            try {
                UUID uuid = UUID.fromString(resultSet.getString(COLUMN_USER_ID.getName()));
                String name = resultSet.getString(COLUMN_USER_NAME.getName());
                long dateCreated = resultSet.getLong(COLUMN_USER_DATE_CREATED.getName());
                long lastOnline = resultSet.getLong(COLUMN_USER_LAST_ONLINE.getName());

                Map<String, SkillData> skillData = new HashMap<>();
                if (Config.LEVELING_ENABLED.get()) {
                    skillData = this.gson.fromJson(resultSet.getString(COLUMN_DATA.getName()), new TypeToken<Map<String, SkillData>>() {}.getType());
                }
                skillData.values().removeIf(Objects::isNull);

                Map<String, ExpirableBooster> boosters = this.gson.fromJson(resultSet.getString(COLUMN_BOOSTERS.getName()), new TypeToken<Map<String, ExpirableBooster>>(){}.getType());
                if (boosters == null) boosters = new HashMap<>();

                UserSettings settings = this.gson.fromJson(resultSet.getString(COLUMN_SETTINGS.getName()), new TypeToken<UserSettings>(){}.getType());
                if (settings == null) settings = new UserSettings();

                return new LootUser(plugin, uuid, name, dateCreated, lastOnline, skillData, boosters, settings);
            }
            catch (SQLException ex) {
                return null;
            }
        };
    }

    @NotNull
    public static DataHandler getInstance(@NotNull LootConomy plugin) {
        if (instance == null) {
            instance = new DataHandler(plugin);
        }
        return instance;
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
        instance = null;
    }

    @Override
    public void onSynchronize() {
        this.plugin.getUserManager().getUsersLoaded().forEach(user -> {
            LootUser fetched = this.getUser(user.getId());
            if (fetched == null) return;

            user.getBoosterMap().clear();
            user.getDataMap().clear();

            user.getBoosterMap().putAll(fetched.getBoosterMap());
            user.getDataMap().putAll(fetched.getDataMap());
        });
    }

    @Override
    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        return super.registerAdapters(builder
            .registerTypeAdapter(UserSettings.class, new UserSettingsSerializer())
            .registerTypeAdapter(SkillData.class, new SkillDataSerializer())
            .registerTypeAdapter(SkillLimitData.class, new SkillLimitSerializer())
            .registerTypeAdapter(BoosterMultiplier.class, new BoosterMultiplierSerializer())
            .registerTypeAdapter(ExpirableBooster.class, new ExpirableBoosterSerializer())
        );
    }

    @Override
    @NotNull
    protected List<SQLColumn> getExtraColumns() {
        return Arrays.asList(COLUMN_DATA, COLUMN_SETTINGS, COLUMN_BOOSTERS);
    }

    @Override
    @NotNull
    protected List<SQLValue> getSaveColumns(@NotNull LootUser user) {
        return Arrays.asList(
            COLUMN_DATA.toValue(this.gson.toJson(user.getDataMap())),
            COLUMN_BOOSTERS.toValue(this.gson.toJson(user.getBoosterMap())),
            COLUMN_SETTINGS.toValue(this.gson.toJson(user.getSettings()))
        );
    }

    @Override
    @NotNull
    protected Function<ResultSet, LootUser> getFunctionToUser() {
        return this.userFunction;
    }

    @NotNull
    public Map<Skill, Map<String, Integer>> getLevels() {
        Map<Skill, Map<String, Integer>> map = new HashMap<>();

        Function<ResultSet, Void> function = resultSet -> {
            try {
                String name = resultSet.getString(COLUMN_USER_NAME.getName());
                Map<String, SkillData> dataMap = gson.fromJson(resultSet.getString(COLUMN_DATA.getName()), new TypeToken<Map<String, SkillData>>(){}.getType());

                this.plugin.getSkillManager().getSkills().forEach(skill -> {
                    SkillData data = dataMap.get(skill.getId());
                    if (data == null) return;

                    map.computeIfAbsent(skill, k -> new HashMap<>()).put(name, data.getLevel());
                });
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        };

        SelectQueryExecutor.builder(this.tableUsers, function)
            .columns(COLUMN_USER_NAME, COLUMN_DATA)
            .execute(this.getConnector());

        /*map.values().forEach(data -> {
            data.put("MoonBunny", Rnd.get(500));
            data.put("7teen", Rnd.get(1200));
            data.put("har1us", Rnd.get(2000));
            data.put("lPariahl", Rnd.get(800));
            data.put("AquaticFlamesIV", Rnd.get(600));
            data.put("YaZanoZa", Rnd.get(200));
            data.put("S_T_I_N_O_L", Rnd.get(400));
            data.put("konoos", Rnd.get(100));
            data.put("ApexDragon", Rnd.get(80));
            data.put("FoX", Rnd.get(1337));
        });*/

        return map;
    }
}
