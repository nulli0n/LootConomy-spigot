package su.nightexpress.lootconomy.config;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.booster.BoosterMultiplier;
import su.nightexpress.lootconomy.booster.config.BoosterInfo;
import su.nightexpress.lootconomy.booster.config.RankBoosterInfo;
import su.nightexpress.lootconomy.booster.config.TimedBoosterInfo;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

public class Config {

    public static final JOption<Set<String>> GENERAL_DISABLED_WORLDS = JOption.create("General.Disabled_Worlds",
        Set.of("my_world", "another_world"),
        "A list of worlds, where no skill XP / currency will be dropped.");

    public static final JOption<Boolean> GENERAL_LOOT_PROTECTION = JOption.create("General.Loot_Protection", true,
        "Sets whether or not loot protection is enabled.",
        "This will prevent players to pickup other player's currency items.");

    public static final JOption<Boolean> GENERAL_LOOT_MERGING = JOption.create("General.Loot_Merging", true,
        "Sets whether or not currency items of the same owner + skill + objective will be merged into one item if nearby.");

    public static final JOption<Boolean> GENERAL_FULL_INV_PICKUP_ENABLED = JOption.create("General.Full_Inventory_Pickup.Enabled",
        true,
        "Sets whether or not players will be able to pickup currency items even with full inventory.");

    public static final JOption<Long> GENERAL_FULL_INV_PICKUP_INTERVAL = JOption.create("General.Full_Inventory_Pickup.Check_Interval",
        30L,
        "Sets how often (in ticks) plugin will check each player with full inventory for currency items around to pickup.",
        "1 second = 20 ticks.",
        "Setting this option to low values may affect server's performance.");

    public static final JOption<Boolean> LEVELING_ENABLED = JOption.create("Leveling.Enabled", true,
        "Sets whether or not leveling is enabled.",
        "WARNING: Disabling this option mid-game will result in all player data lost!");

    public static final JOption<Boolean> LEVELING_FIREWORKS = JOption.create("Leveling.Fireworks", true,
        "Sets whether or not a random firework will be spawned above the player on skill level up.");

    public static final JOption<Set<CreatureSpawnEvent.SpawnReason>> EXPLOIT_IGNORE_SPAWN_REASONS = JOption.forSet("Exploit_Protection.Ignore_SpawnReasons",
        raw -> StringUtil.getEnum(raw, CreatureSpawnEvent.SpawnReason.class).orElse(null),
        Set.of(
            CreatureSpawnEvent.SpawnReason.EGG,
            CreatureSpawnEvent.SpawnReason.SPAWNER,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
            CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
            CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN,
            CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM,
            CreatureSpawnEvent.SpawnReason.SLIME_SPLIT
        ),
        "A list of SpawnReasons that will make mobs spawned by them drop no skill XP / currency.",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html"
    ).setWriter((cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()));

    public static final JOption<Set<GameMode>> EXPLOIT_IGNORE_GAME_MODES = JOption.forSet("Exploit_Protection.Ignore_GameModes",
        raw -> StringUtil.getEnum(raw, GameMode.class).orElse(null),
        Set.of(GameMode.CREATIVE),
        "A list of player GameModes where no skill XP / currency will be dropped.",
        "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/GameMode.html"
    ).setWriter((cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()));

    public static final JOption<Set<Material>> EXPLOIT_IGNORE_BLOCK_GENERATION = JOption.forSet("Exploit_Protection.Ignore_Block_Generation",
        raw -> Material.getMaterial(raw.toUpperCase()),
        Set.of(
            Material.STONE,
            Material.COBBLESTONE,
            Material.OBSIDIAN
        ),
        "All blocks types listed below, that are generated/formed by the world mechanics will drop no skill XP / currency."
    ).setWriter((cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()));

    public static final JOption<Set<Material>> EXPLOIT_IGNORE_FERTILIZED = JOption.forSet("Exploit_Protection.Ignore_Fertilized",
        raw -> Material.getMaterial(raw.toUpperCase()),
        Set.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS
        ),
        "A list of blocks that will drop no skill XP / currency if been fertilized by bone meal.",
        "To get a valid block name, press F3 and look at a block. Use name wihout 'minecraft:' prefix.",
        Placeholders.URL_SPIGOT_MATERIAL
    ).setWriter((cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList()));

    public static final JOption<Integer> TOP_UPDATE_INTERVAL = JOption.create("Top.Update_Interval", 600,
        "Sets how often (in seconds) top level leaderboard will be fetched and updated.");

    public static final JOption<Integer> TOP_ENTRIES_PER_PAGE = JOption.create("Top.Entries_Per_Page", 10,
        "Sets how many entries per leaderboard page will be displated.");

    public static final JOption<Map<String, TimedBoosterInfo>> BOOSTERS_GLOBAL = JOption.forMap("Boosters.Global",
        (cfg, path, id) -> TimedBoosterInfo.read(cfg, path + "." + id),
        Map.of(
            "example", new TimedBoosterInfo(Set.of(Placeholders.WILDCARD),
                new BoosterMultiplier(Map.of(EngineUtils.VAULT, 1.25), 1.25),
                Map.of(DayOfWeek.SATURDAY, Set.of(LocalTime.of(16, 0))), 7200)
        ),
        "List of global, automated XP / currency boosters.",
        "You can create as many boosters as you want.",
        "But keep in mind that only one global booster can be active at the same time.",
        "If you have multiple boosters applicable at the same day times, the latest one will override all previous.")
        .setWriter((cfg, path, map) -> map.forEach((id, info) -> info.write(cfg, path + "." + id)));

    public static final JOption<Map<String, RankBoosterInfo>> BOOSTERS_RANK = JOption.forMap("Boosters.Rank",
        (cfg, path, id) -> RankBoosterInfo.read(cfg, path + "." + id, id),
        Map.of(
            "vip", new RankBoosterInfo("vip", 10, Set.of(Placeholders.WILDCARD),
                new BoosterMultiplier(Map.of(EngineUtils.VAULT, 1.25), 1.25)
            ),
            "premium", new RankBoosterInfo("premium", 10, Set.of(Placeholders.WILDCARD),
                new BoosterMultiplier(Map.of(EngineUtils.VAULT, 1.5), 1.5)
            )
        ),
        "List of passive XP / currency boosters based on player permission group(s).",
        "Use the 'Priority' option to define booster's priority to guarantee that players with multiple permission groups will get the best one.")
        .setWriter((cfg, path, map) -> map.forEach((id, info) -> info.write(cfg, path + "." + id)));

    public static final JOption<Map<String, BoosterInfo>> BOOSTERS_CUSTOM = JOption.forMap("Boosters.Custom",
        (cfg, path, id) -> BoosterInfo.read(cfg, path + "." + id),
        Map.of(
            "xp_money_1_25", new BoosterInfo(Set.of(Placeholders.WILDCARD),
                new BoosterMultiplier(Map.of(EngineUtils.VAULT, 1.25), 1.25)),
            "xp_money_1_5", new BoosterInfo(Set.of(Placeholders.WILDCARD),
                new BoosterMultiplier(Map.of(EngineUtils.VAULT, 1.5), 1.5)),
            "money_2", new BoosterInfo(Set.of(Placeholders.WILDCARD),
                new BoosterMultiplier(Map.of(EngineUtils.VAULT, 2D), 1D)),
            "xp_2", new BoosterInfo(Set.of(Placeholders.WILDCARD),
                new BoosterMultiplier(Map.of(EngineUtils.VAULT, 1D), 2D))
        ),
        "List of custom XP / currency boosters to be given via booster commands.",
        "You can create as many boosters as you want.",
        "But keep in mind that only one personal booster per skill can be active at the same time.",
        "If player already has a booster for certain skill, it will be replaced with a new one.")
        .setWriter((cfg, path, map) -> map.forEach((id, info) -> info.write(cfg, path + "." + id)));
}
