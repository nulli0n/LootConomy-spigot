package su.nightexpress.lootconomy.config;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.config.RankBoosterInfo;
import su.nightexpress.lootconomy.booster.config.ScheduledBoosterInfo;
import su.nightexpress.lootconomy.currency.handler.VaultEconomyHandler;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.StringUtil;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.lootconomy.Placeholders.*;

public class Config {

    public static final String DIR_UI         = "/ui/";
    public static final String DIR_OBJECTIVES = "/objectives/";

    public static final ConfigValue<Set<String>> DISABLED_WORLDS = ConfigValue.create("General.Disabled_Worlds",
        Set.of("my_world", "another_world"),
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
        Set.of(
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
        Set.of(GameMode.CREATIVE),
        "Disables currency drops for players in certain gamemodes.",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/GameMode.html"
    );

    public static final ConfigValue<Set<Material>> ABUSE_IGNORE_BLOCK_GENERATION = ConfigValue.forSet("Abuse_Protection.Ignore_Block_Generation",
        BukkitThing::getMaterial,
        (cfg, path, set) -> cfg.set(path, set.stream().map(BukkitThing::toString).toList()),
        Set.of(
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
        Set.of(
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

    /*public static final ConfigValue<Integer> TOP_UPDATE_INTERVAL = ConfigValue.create("Top.Update_Interval",
        600,
        "Sets how often (in seconds) top level leaderboard will be fetched and updated.");

    public static final ConfigValue<Integer> TOP_ENTRIES_PER_PAGE = ConfigValue.create("Top.Entries_Per_Page",
        10,
        "Sets how many entries per leaderboard page will be displated.");*/

    public static final ConfigValue<String> BOOSTER_FORMAT_POSITIVE = ConfigValue.create("Boosters.Format.Positive",
        LIGHT_GREEN.enclose("+" + GENERIC_AMOUNT + "%"));

    public static final ConfigValue<String> BOOSTER_FORMAT_NEGATIVE = ConfigValue.create("Boosters.Format.Negative",
        LIGHT_RED.enclose(GENERIC_AMOUNT + "%"));

    public static final ConfigValue<Integer> BOOSTER_SCHEDULER_INTERVAL = ConfigValue.create("Boosters.Scheduler_Interval",
        30,
        "Sets how often (in seconds) plugin will check out for available scheduled boosters (see below).",
        "Setting this above 60 may cause scheduled boosters to not activate properly sometimes.",
        "[Default is 30]");

    public static final ConfigValue<Map<String, ScheduledBoosterInfo>> BOOSTERS_SCHEDULED = ConfigValue.forMap("Boosters.Scheduled",
        (cfg, path, id) -> ScheduledBoosterInfo.read(cfg, path + "." + id),
        (cfg, path, map) -> map.forEach((id, info) -> info.write(cfg, path + "." + id)),
        Map.of(
            "example",
            new ScheduledBoosterInfo(
                new Multiplier(Map.of(VaultEconomyHandler.ID, 1.25)),
                Map.of(DayOfWeek.SATURDAY, Set.of(LocalTime.of(16, 0))),
                7200
            )
        ),
        "List of global currency boosters scheduled for certain times.",
        "You can create as many boosters as you want."
    );

    public static final ConfigValue<Map<String, RankBoosterInfo>> BOOSTERS_RANK = ConfigValue.forMap("Boosters.Rank",
        (cfg, path, id) -> RankBoosterInfo.read(cfg, path + "." + id, id),
        (cfg, path, map) -> map.forEach((id, info) -> info.write(cfg, path + "." + id)),
        Map.of(
            "vip", new RankBoosterInfo("vip", 10,
                new Multiplier(Map.of(VaultEconomyHandler.ID, 1.25))
            ),
            "premium", new RankBoosterInfo("premium", 20,
                new Multiplier(Map.of(VaultEconomyHandler.ID, 1.5))
            )
        ),
        "List of persistent currency boosters based on player permission group(s).",
        "Use the 'Priority' option to define booster's priority to guarantee that players with multiple permission groups will get the best one."
    );

    /*@Deprecated
    public static final ConfigValue<Map<String, BoosterInfo>> BOOSTERS_CUSTOM = ConfigValue.forMap("Boosters.Custom",
        (cfg, path, id) -> BoosterInfo.read(cfg, path + "." + id),
        (cfg, path, map) -> map.forEach((id, info) -> info.write(cfg, path + "." + id)),
        Map.of(
            "xp_money_1_25", new BoosterInfo(new Multiplier(Map.of(VaultEconomyHandler.ID, 1.25))),
            "xp_money_1_5", new BoosterInfo(new Multiplier(Map.of(VaultEconomyHandler.ID, 1.5))),
            "money_2", new BoosterInfo(new Multiplier(Map.of(VaultEconomyHandler.ID, 2D))),
            "xp_2", new BoosterInfo(new Multiplier(Map.of(VaultEconomyHandler.ID, 1D)))
        ),
        "List of custom XP / currency boosters to be given via booster commands.",
        "You can create as many boosters as you want.",
        "But keep in mind that only one personal booster per skill can be active at the same time.",
        "If player already has a booster for certain skill, it will be replaced with a new one."
    );*/
}
