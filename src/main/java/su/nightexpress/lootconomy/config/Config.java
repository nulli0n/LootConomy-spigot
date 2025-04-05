package su.nightexpress.lootconomy.config;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.lootconomy.booster.BoosterUtils;
import su.nightexpress.lootconomy.booster.impl.BoosterSchedule;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.rankmap.DoubleRankMap;
import su.nightexpress.nightcore.util.rankmap.RankMap;

import java.util.Map;
import java.util.Set;

import static su.nightexpress.lootconomy.Placeholders.URL_DATA_VALUES;
import static su.nightexpress.lootconomy.Placeholders.URL_ECO_BRIDGE;

public class Config {

    public static final String DIR_UI         = "/ui/";
    public static final String DIR_OBJECTIVES = "/objectives/";

    public static final ConfigValue<Set<String>> DISABLED_WORLDS = ConfigValue.create("General.Disabled_Worlds",
        Lists.newSet("my_world", "another_world"),
        "A list of worlds, where no currency will be dropped.");

    public static final ConfigValue<Boolean> LOOT_PROTECTION = ConfigValue.create("Loot.Protection.Enabled",
        true,
        "Sets whether or not loot protection is enabled.",
        "This will prevent players to pickup other player's currency items.");

    public static final ConfigValue<Boolean> LOOT_MERGE_ENABLED = ConfigValue.create("Loot.Merging.Enabled",
        false,
        "Sets whether or not currency items will be merged into one item when nearby to each other.");

    public static final ConfigValue<Integer> LOOT_MERGE_INTERVAL = ConfigValue.create("Loot.Merging.Interval",
        2,
        "Sets seconds interval for item merging.",
        "[Default is 2]");

    public static final ConfigValue<Boolean> INVENTORY_SIZE_BYPASS_ENABLED = ConfigValue.create("Loot.Full_Inventory_Pickup.Enabled",
        true,
        "Sets whether or not players will be able to pickup currency items even with full inventory.");

    public static final ConfigValue<Long> INVENTORY_SIZE_BYPASS_INTERVAL = ConfigValue.create("Loot.Full_Inventory_Pickup.Check_Interval",
        30L,
        "Sets how often (in ticks) plugin will check each player with full inventory for currency items around to pickup.",
        "1 second = 20 ticks.",
        "Setting this option to low values may damage server's performance.");
//
//    public static final ConfigValue<Map<String, ObjectiveCategory>> OBJECTIVE_CATEGORIES = ConfigValue.forMapById("Objectives.Categories",
//        (cfg, path) -> ObjectiveCategory.read(cfg, path))

    public static final ConfigValue<Boolean> OBJECTIVE_CUSTOM_MULTIPLIERS_ENABLED = ConfigValue.create("Objectives.Custom_Multipliers.Enabled",
        false,
        "Enables Custom Multipliers feature.");

    public static final ConfigValue<Map<String, String>> OBJECTIVE_CUSTOM_MULTIPLIERS_LIST = ConfigValue.forMap("Objectives.Custom_Multipliers.List",
        (cfg, path, id) -> cfg.getString(path + "." + id, ""),
        (cfg, path, map) -> map.forEach((id, val) -> cfg.set(path + "." + id, val)),
        () -> Map.of("level", "%player_level%"),
        "Here you can define custom multipliers for currency objectives using " + Plugins.PLACEHOLDER_API + " placeholders.",
        "Placeholder MUST return numeric value to work properly!",
        "Assign your custom multiplier(s) to currency objectives in their configs in " + Config.DIR_OBJECTIVES + " directory."
    );

    public static final ConfigValue<Set<CreatureSpawnEvent.SpawnReason>> ABUSE_IGNORE_SPAWN_REASONS = ConfigValue.forSet("Abuse_Protection.Ignore_SpawnReasons",
        raw -> StringUtil.getEnum(raw, CreatureSpawnEvent.SpawnReason.class).orElse(null),
        (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
        Lists.newSet(
            CreatureSpawnEvent.SpawnReason.EGG,
            CreatureSpawnEvent.SpawnReason.SPAWNER,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
            CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
            CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN,
            CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
            CreatureSpawnEvent.SpawnReason.SLIME_SPLIT
        ),
        "Disables currency drops from all mobs that were spawned under certain conditions.",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html"
    );

    public static final ConfigValue<Set<GameMode>> ABUSE_IGNORE_GAME_MODES = ConfigValue.forSet("Abuse_Protection.Ignore_GameModes",
        raw -> StringUtil.getEnum(raw, GameMode.class).orElse(null),
        (cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()),
        Lists.newSet(GameMode.CREATIVE),
        "Disables currency drops for players in certain gamemodes.",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/GameMode.html"
    );

    public static final ConfigValue<Set<Material>> ABUSE_IGNORE_BLOCK_GENERATION = ConfigValue.forSet("Abuse_Protection.Ignore_Block_Generation",
        BukkitThing::getMaterial,
        (cfg, path, set) -> cfg.set(path, set.stream().map(BukkitThing::toString).toList()),
        Lists.newSet(
            Material.STONE,
            Material.COBBLESTONE,
            Material.OBSIDIAN
        ),
        "Disables currency drops from certain blocks that were generated/formed by the world mechanics (e.g. obsidian/cobblestone generators).",
        URL_DATA_VALUES + " -> Blocks -> Show -> Resource location"
    );

    public static final ConfigValue<Set<Material>> ABUSE_IGNORE_FERTILIZED = ConfigValue.forSet("Abuse_Protection.Ignore_Fertilized",
        BukkitThing::getMaterial,
        (cfg, path, set) -> cfg.set(path, set.stream().map(BukkitThing::toString).toList()),
        Lists.newSet(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS
        ),
        "Disables currency drops from certain blocks that were fertilized by bone meal.",
        URL_DATA_VALUES + " -> Blocks -> Show -> Resource location"
    );

    public static final ConfigValue<Boolean> ABUSE_NO_DROP_FROM_TAMED_MOBS = ConfigValue.create("Abuse_Protection.No_Drop_From_Tamed_Mobs",
        true,
        "Disables currency drop from tamed mobs.",
        "Not really an abuse, just a QoL (Quality of Life) feature."
    );

    public static final ConfigValue<Integer> BOOSTER_TICK_INTERVAL = ConfigValue.create("Boosters.Tick_Interval",
        1,
        "Sets booster update interval.",
        "[*] Do not change unless you understand what you're doing.",
        "[Asynchronous]",
        "[Default is 1]");

    public static final ConfigValue<Set<String>> BOOSTER_EXCLUSIVE_CURRENCIES = ConfigValue.create("Boosters.Exclusives.Currency",
        Lists.newSet(CurrencyId.forCoinsEngine("super_coins"), "some_currency"),
        "Boosters will have no effect on listed currencies.",
        URL_ECO_BRIDGE
    ).onRead(set -> Lists.modify(set, String::toLowerCase));

    public static final ConfigValue<Map<String, BoosterSchedule>> BOOSTERS_BY_SCHEDULE = ConfigValue.forMapById("Boosters.BySchedule",
        BoosterSchedule::read,
        map -> map.putAll(BoosterUtils.getDefaultBoosterSchedules()),
        "Global currency boosters that activates at specific day times.",
        "You can define as many as you want."
    );

    public static final ConfigValue<RankMap<Double>> BOOSTERS_BY_RANK = ConfigValue.create("Boosters.ByRank",
        DoubleRankMap::read,
        (cfg, path, map) -> map.write(cfg, path),
        () -> DoubleRankMap.permissioned(Perms.PREFIX + "rankbooster.", 1D).addValue("vip", 1.5D).addValue("pro", 2D),
        "Persistent currency boosters applied to players based on their rank or permissions."
    );

    @NotNull
    public static Map<String, BoosterSchedule> getBoosterScheduleMap() {
        return BOOSTERS_BY_SCHEDULE.get();
    }
}
