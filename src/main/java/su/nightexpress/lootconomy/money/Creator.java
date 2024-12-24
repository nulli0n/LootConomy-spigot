package su.nightexpress.lootconomy.money;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.loot.handler.LootAction;
import su.nightexpress.lootconomy.loot.handler.LootActions;
import su.nightexpress.lootconomy.loot.objective.ObjectiveCategory;
import su.nightexpress.lootconomy.money.object.DropInfo;
import su.nightexpress.lootconomy.money.object.MoneyObjective;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.wrapper.UniDouble;
import su.nightexpress.nightcore.util.wrapper.UniInt;

import java.io.File;
import java.util.*;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class Creator {

    private static final UniDouble MONEY_LOWEST      = UniDouble.of(0.5, 1.5);
    private static final UniDouble MONEY_LOW         = UniDouble.of(2.5, 5);
    private static final UniDouble MONEY_LOW_MEDIUM  = UniDouble.of(5, 10);
    private static final UniDouble MONEY_MEDIUM      = UniDouble.of(7, 15);
    private static final UniDouble MONEY_MEDIUM_HIGH = UniDouble.of(15, 30);
    private static final UniDouble MONEY_HIGH        = UniDouble.of(20, 50);
    private static final UniDouble MONEY_HIGHEST     = UniDouble.of(100, 500);
    private static final UniDouble MONEY_BOSS        = UniDouble.of(5000, 25000);

    private static final UniDouble PENALTY_MEDIUM = UniDouble.of(-7, -15);

    private final LootConomyPlugin plugin;

    public Creator(@NotNull LootConomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void create() {
        this.createCategories();
        this.createObjectives();
    }

    private void createObjectives() {
        File dir = new File(this.plugin.getDataFolder(), Config.DIR_OBJECTIVES);
        if (dir.exists()) return;

        dir.mkdirs();

        this.createForMining();
        this.createForGathering();
        this.createForFishing();
        this.createForMobKill();
        this.createForShearing();
    }

    private void createCategories() {
        FileConfig config = this.plugin.getConfig();
        if (config.contains("Objectives.Categories")) return;

        this.createCategory(LootActions.MINING, Lists.newList(
            LIGHT_GRAY.enclose("Blocks that drop money"),
            LIGHT_GRAY.enclose("when mined.")
        ), new NightItem(Material.COBBLESTONE));

        this.createCategory(LootActions.GATHERING, Lists.newList(
            LIGHT_GRAY.enclose("Plants that drop money"),
            LIGHT_GRAY.enclose("when harvested.")
        ), new NightItem(Material.WHEAT));

        this.createCategory(LootActions.MOB_KILL, Lists.newList(
            LIGHT_GRAY.enclose("Mobs that drop money"),
            LIGHT_GRAY.enclose("when killed.")
        ), new NightItem(Material.ZOMBIE_HEAD));

        this.createCategory(LootActions.SHEARING, Lists.newList(
            LIGHT_GRAY.enclose("Mobs that drop money"),
            LIGHT_GRAY.enclose("when sheared.")
        ), new NightItem(Material.SHEARS));

        this.createCategory(LootActions.FISHING, Lists.newList(
            LIGHT_GRAY.enclose("Items that drop money"),
            LIGHT_GRAY.enclose("when fished.")
        ), new NightItem(Material.FISHING_ROD));
    }

    private void createCategory(@NotNull LootAction<?, ?> action, @NotNull List<String> description, @NotNull NightItem icon) {
        String id = action.getDefaultCategory();
        String name = StringUtil.capitalizeUnderscored(id);

        ObjectiveCategory category = new ObjectiveCategory(id, name, description, icon);
        category.write(this.plugin.getConfig(), "Objectives.Categories." + category.getId());
    }

    private void createForMining() {
        var type = LootActions.MINING;

        Set<MoneyObjective> stones = new HashSet<>();
        Set<MoneyObjective> ores = new HashSet<>();
        Set<MoneyObjective> dirt = new HashSet<>();
        Set<MoneyObjective> logs = new HashSet<>();

        stones.add(build("stones", type, Tag.BASE_STONE_OVERWORLD, new NightItem(Material.STONE), 0.3, MONEY_LOWEST));
        stones.add(build("nether_stones", type, Tag.BASE_STONE_NETHER, new NightItem(Material.NETHERRACK), 0.1, MONEY_LOWEST));
        stones.add(build("nylium", type, Tag.NYLIUM, new NightItem(Material.CRIMSON_NYLIUM), 0.2, MONEY_LOWEST));
        stones.add(build("terracotta", type, Tag.TERRACOTTA, new NightItem(Material.TERRACOTTA), 0.5, MONEY_LOWEST));

        ores.add(build("coal_ores", type, Tag.COAL_ORES, new NightItem(Material.COAL_ORE), 35, MONEY_LOW));
        ores.add(build("copper_ores", type, Tag.COPPER_ORES, new NightItem(Material.COPPER_ORE), 45, MONEY_LOW_MEDIUM));
        ores.add(build("diamond_ores", type, Tag.DIAMOND_ORES, new NightItem(Material.DIAMOND_ORE), 80, MONEY_HIGH));
        ores.add(build("emerald_ores", type, Tag.EMERALD_ORES, new NightItem(Material.EMERALD_ORE), 100, MONEY_HIGHEST));
        ores.add(build("redstone_ores", type, Tag.REDSTONE_ORES, new NightItem(Material.REDSTONE_ORE), 50, MONEY_MEDIUM));
        ores.add(build("lapis_ores", type, Tag.LAPIS_ORES, new NightItem(Material.LAPIS_ORE), 50, MONEY_MEDIUM));
        ores.add(build("iron_ores", type, Tag.IRON_ORES, new NightItem(Material.IRON_ORE), 40, MONEY_LOW_MEDIUM));
        ores.add(build("nether_quartz_ore", type, Material.NETHER_QUARTZ_ORE, new NightItem(Material.NETHER_QUARTZ_ORE), 75, MONEY_LOW_MEDIUM));
        ores.add(build("nether_gold_ore", type, Material.NETHER_GOLD_ORE, new NightItem(Material.NETHER_GOLD_ORE), 100, MONEY_MEDIUM));

        dirt.add(build("dirt", type, Tag.DIRT, new NightItem(Material.DIRT), 0.1, MONEY_LOWEST));
        dirt.add(build("sand", type, Tag.SAND, new NightItem(Material.SAND), 0.1, MONEY_LOWEST));
        dirt.add(build("snow", type, Tag.SNOW, new NightItem(Material.SNOW_BLOCK), 0.1, MONEY_LOWEST));
        dirt.add(build("clay", type, Material.CLAY, new NightItem(Material.CLAY), 5, MONEY_LOW));
        dirt.add(build("gravel", type, Material.GRAVEL, new NightItem(Material.GRAVEL), 5, MONEY_LOW));
        dirt.add(build("soul_sand", type, Material.SOUL_SAND, new NightItem(Material.SOUL_SAND), 1, MONEY_LOW));
        dirt.add(build("mycelium", type, Material.MYCELIUM, new NightItem(Material.MYCELIUM), 0.2, MONEY_LOW));

        logs.add(build("acacia_logs", type, Tag.ACACIA_LOGS, new NightItem(Material.ACACIA_LOG), 10, MONEY_LOW));
        logs.add(build("birch_logs", type, Tag.BIRCH_LOGS, new NightItem(Material.BIRCH_LOG), 15, MONEY_LOW));
        logs.add(build("crimson_stems", type, Tag.CRIMSON_STEMS, new NightItem(Material.CRIMSON_STEM), 15, MONEY_LOW));
        logs.add(build("dark_oak_logs", type, Tag.DARK_OAK_LOGS, new NightItem(Material.DARK_OAK_LOG), 15, MONEY_LOW));
        logs.add(build("jungle_logs", type, Tag.JUNGLE_LOGS, new NightItem(Material.JUNGLE_LOG), 20, MONEY_LOW));
        logs.add(build("mangrove_logs", type, Tag.MANGROVE_LOGS, new NightItem(Material.MANGROVE_LOG), 15, MONEY_LOW));
        logs.add(build("oak_logs", type, Tag.OAK_LOGS, new NightItem(Material.OAK_LOG), 10, MONEY_LOW));
        logs.add(build("spruce_logs", type, Tag.SPRUCE_LOGS, new NightItem(Material.SPRUCE_LOG), 10, MONEY_LOW));
        logs.add(build("warped_stems", type, Tag.WARPED_STEMS, new NightItem(Material.WARPED_STEM), 15, MONEY_LOW));
        logs.add(build("leaves", type, Tag.LEAVES, new NightItem(Material.OAK_LEAVES), 3, MONEY_LOWEST));
        logs.add(build("cherry_logs", type, Tag.CHERRY_LOGS, new NightItem(Material.CHERRY_LOG), 15, MONEY_LOW));

        writeConfig("blocks_stones", stones);
        writeConfig("blocks_ores", ores);
        writeConfig("blocks_dirt", dirt);
        writeConfig("blocks_logs", logs);
    }

    private void createForGathering() {
        var type = LootActions.MINING;
        var harvest = LootActions.GATHERING;

        Set<MoneyObjective> crops = new HashSet<>();
        Set<MoneyObjective> berries = new HashSet<>();

        crops.add(build("pumpkin", type, Material.PUMPKIN, new NightItem(Material.PUMPKIN), 100, MONEY_LOW));
        crops.add(build("melon", type, Material.MELON, new NightItem(Material.MELON), 100, MONEY_LOW));
        crops.add(build("beetroot", type, Material.BEETROOTS, new NightItem(Material.BEETROOT), 100, MONEY_LOW));
        crops.add(build("carrot", type, Material.CARROTS, new NightItem(Material.CARROT), 100, MONEY_LOW));
        crops.add(build("potato", type, Material.POTATOES, new NightItem(Material.POTATO), 100, MONEY_LOW));
        crops.add(build("wheat", type, Material.WHEAT, new NightItem(Material.WHEAT), 100, MONEY_LOW));
        crops.add(build("nether_wart", type, Material.NETHER_WART, new NightItem(Material.NETHER_WART), 100, MONEY_LOW));
        crops.add(build("red_mushroom", type, Material.RED_MUSHROOM, new NightItem(Material.RED_MUSHROOM), 100, MONEY_LOW));
        crops.add(build("brown_mushroom", type, Material.BROWN_MUSHROOM, new NightItem(Material.BROWN_MUSHROOM), 100, MONEY_LOW));
        crops.add(build("sugar_cane", type, Material.SUGAR_CANE, new NightItem(Material.SUGAR_CANE), 100, MONEY_LOW));
        crops.add(build("bamboo", type, Material.BAMBOO, new NightItem(Material.BAMBOO), 100, MONEY_LOW));

        berries.add(build("sweet_berries", harvest, Material.SWEET_BERRIES, new NightItem(Material.SWEET_BERRIES), 100, MONEY_LOW));
        berries.add(build("glow_berries", harvest, Material.GLOW_BERRIES, new NightItem(Material.GLOW_BERRIES), 100, MONEY_LOW));

        writeConfig("gather_crops", crops);
        writeConfig("gather_berries", berries);
    }

    private void createForFishing() {
        var type = LootActions.FISHING;

        Set<MoneyObjective> fishes = new HashSet<>();
        Set<MoneyObjective> treasures = new HashSet<>();
        Set<MoneyObjective> junk = new HashSet<>();

        fishes.add(build("cod", type, Material.COD, new NightItem(Material.COD), 100, MONEY_LOW));
        fishes.add(build("salmon", type, Material.SALMON, new NightItem(Material.SALMON), 100, MONEY_LOW_MEDIUM));
        fishes.add(build("tropical_fish", type, Material.TROPICAL_FISH, new NightItem(Material.TROPICAL_FISH), 100, MONEY_LOW_MEDIUM));
        fishes.add(build("pufferfish", type, Material.PUFFERFISH, new NightItem(Material.PUFFERFISH), 100, MONEY_LOW_MEDIUM));

        treasures.add(build("bow", type, Material.BOW, new NightItem(Material.BOW), 100, MONEY_MEDIUM));
        treasures.add(build("enchanted_book", type, Material.ENCHANTED_BOOK, new NightItem(Material.ENCHANTED_BOOK), 100, MONEY_HIGH));
        treasures.add(build("fishing_rod", type, Material.FISHING_ROD, new NightItem(Material.FISHING_ROD), 100, MONEY_LOW_MEDIUM));
        treasures.add(build("name_tag", type, Material.NAME_TAG, new NightItem(Material.NAME_TAG), 100, MONEY_MEDIUM));
        treasures.add(build("nautilus_shell", type, Material.NAUTILUS_SHELL, new NightItem(Material.NAUTILUS_SHELL), 100, MONEY_MEDIUM));
        treasures.add(build("saddle", type, Material.SADDLE, new NightItem(Material.SADDLE), 100, MONEY_MEDIUM));

        junk.add(build("lily_pad", type, Material.LILY_PAD, new NightItem(Material.LILY_PAD), 100, MONEY_LOW));
        junk.add(build("bowl", type, Material.BOWL, new NightItem(Material.BOWL), 100, MONEY_LOW));
        junk.add(build("leather", type, Material.LEATHER, new NightItem(Material.LEATHER), 100, MONEY_LOW));
        junk.add(build("leather_boots", type, Material.LEATHER_BOOTS, new NightItem(Material.LEATHER_BOOTS), 100, MONEY_LOW));
        junk.add(build("rotten_flesh", type, Material.ROTTEN_FLESH, new NightItem(Material.ROTTEN_FLESH), 100, MONEY_LOW));
        junk.add(build("stick", type, Material.STICK, new NightItem(Material.STICK), 100, MONEY_LOW));
        junk.add(build("string", type, Material.STRING, new NightItem(Material.STRING), 100, MONEY_LOW));
        junk.add(build("potion", type, Material.POTION, new NightItem(Material.POTION), 100, MONEY_LOW));
        junk.add(build("bone", type, Material.BONE, new NightItem(Material.BONE), 100, MONEY_LOW));
        junk.add(build("ink_sac", type, Material.INK_SAC, new NightItem(Material.INK_SAC), 100, MONEY_LOW));
        junk.add(build("tripwire_hook", type, Material.TRIPWIRE_HOOK, new NightItem(Material.TRIPWIRE_HOOK), 100, MONEY_LOW));

        writeConfig("fishing_fishes", fishes);
        writeConfig("fishing_treasures", treasures);
        writeConfig("fishing_junk", junk);
    }

    private void createForMobKill() {
        var type = LootActions.MOB_KILL;

        Set<MoneyObjective> penalty = new HashSet<>();
        Set<MoneyObjective> raiders = new HashSet<>();
        Set<MoneyObjective> skeletons = new HashSet<>();
        Set<MoneyObjective> animals = new HashSet<>();
        Set<MoneyObjective> zombies = new HashSet<>();
        Set<MoneyObjective> fishes = new HashSet<>();
        Set<MoneyObjective> various = new HashSet<>();
        Set<MoneyObjective> bosses = new HashSet<>();

        penalty.add(build("dolphin", type, EntityType.DOLPHIN, "8e9688b950d880b55b7aa2cfcd76e5a0fa94aac6d16f78e833f7443ea29fed3", 100, PENALTY_MEDIUM));
        penalty.add(build("bee", type, EntityType.BEE, "cce9edbbc5fdc0d8487ac72eab239d2cacfe408d74288d6384b044111ba4de0f", 100, PENALTY_MEDIUM));
        penalty.add(build("ocelot", type, EntityType.OCELOT, "5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1", 100, PENALTY_MEDIUM));
        penalty.add(build("horse", type, EntityType.HORSE, "a996399fff9cbcfb7ba677dd0c2d104229d1cc2307a6f075a882da4694ef80ae", 100, PENALTY_MEDIUM));
        penalty.add(build("mule", type, EntityType.MULE, "a0486a742e7dda0bae61ce2f55fa13527f1c3b334c57c034bb4cf132fb5f5f", 100, PENALTY_MEDIUM));
        penalty.add(build("donkey", type, EntityType.DONKEY, "63a976c047f412ebc5cb197131ebef30c004c0faf49d8dd4105fca1207edaff3", 100, PENALTY_MEDIUM));
        penalty.add(build("villager", type, EntityType.VILLAGER, "a36e9841794a37eb99524925668b47a62b5cb72e096a9f8f95e106804ae13e1b", 100, PENALTY_MEDIUM));
        penalty.add(build("wandering_trader", type, EntityType.WANDERING_TRADER, "ee011aac817259f2b48da3e5ef266094703866608b3d7d1754432bf249cd2234", 100, PENALTY_MEDIUM));
        penalty.add(build("turtle", type, EntityType.TURTLE, "8fa552139966c5fac1b98061ce23fc0ddef058c163142dd6d1c768cd2da207c2", 100, PENALTY_MEDIUM));
        penalty.add(build("cat", type, EntityType.CAT, "d0dba942c06b77a2828e3f66a1faec5e8643e9ea61a81a4523279739ed82d", 100, PENALTY_MEDIUM));
        penalty.add(build("llama", type, EntityType.LLAMA, "9f7d90b305aa64313c8d4404d8d652a96eba8a754b67f4347dcccdd5a6a63398", 100, PENALTY_MEDIUM));
        penalty.add(build("trader_llama", type, EntityType.TRADER_LLAMA, "8424780b3c5c5351cf49fb5bf41fcb289491df6c430683c84d7846188db4f84d", 100, PENALTY_MEDIUM));
        penalty.add(build("iron_golem", type, EntityType.IRON_GOLEM, "a9ceb73d97cf5dc32e333dbef7af25f39e42033d684649075ba4681af2a3c01b", 100, PENALTY_MEDIUM));
        penalty.add(build("snowman", type, EntityType.SNOW_GOLEM, "8e8d206f61e6de8a79d0cb0bcd98aced464cbfefc921b4160a25282163112a", 100, PENALTY_MEDIUM));
        penalty.add(build("sniffer", type, EntityType.SNIFFER, "fe5a8341c478a134302981e6a7758ea4ecfd8d62a0df4067897e75502f9b25de", 100, PENALTY_MEDIUM));
        penalty.add(build("camel", type, EntityType.CAMEL, "ba4c95bfa0b61722255389141b505cf1a38bad9b0ef543de619f0cc9221ed974", 100, PENALTY_MEDIUM));

        animals.add(build("pig", type, EntityType.PIG, "fa305e321e87ec91421ecccf7cfef10703fb77f62658d6b998f117fcf34cd0b2", 30, MONEY_LOW));
        animals.add(build("cow", type, EntityType.COW, "b667c0e107be79d7679bfe89bbc57c6bf198ecb529a3295fcfdfd2f24408dca3", 30, MONEY_LOW));
        animals.add(build("sheep", type, EntityType.SHEEP, "a723893df4cfb9c7240fc47b560ccf6ddeb19da9183d33083f2c71f46dad290a", 30, MONEY_LOW));
        animals.add(build("goat", type, EntityType.GOAT, "457a0d538fa08a7affe312903468861720f9fa34e86d44b89dcec5639265f03", 30, MONEY_LOW));
        animals.add(build("chicken", type, EntityType.CHICKEN, "1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893", 30, MONEY_LOW));
        animals.add(build("mushroom_cow", type, EntityType.MOOSHROOM, "45603d539f666fdf0f7a0fe20b81dfef3abe6c51da34b9525a5348432c5523b2", 30, MONEY_LOW));
        animals.add(build("fox", type, EntityType.FOX, "d8954a42e69e0881ae6d24d4281459c144a0d5a968aed35d6d3d73a3c65d26a", 30, MONEY_LOW));
        animals.add(build("frog", type, EntityType.FROG, "45852a95928897746012988fbd5dbaa1b70b7a5fb65157016f4ff3f245374c08", 30, MONEY_LOW));
        animals.add(build("panda", type, EntityType.PANDA, "8018a1771d69c11b8dad42cd310375ba2d827932b25ef357f7e572c1bd0f9", 30, MONEY_LOW));
        animals.add(build("parrot", type, EntityType.PARROT, "a4ba8d66fecb1992e94b8687d6ab4a5320ab7594ac194a2615ed4df818edbc3", 30, MONEY_LOW));
        animals.add(build("polar_bear", type, EntityType.POLAR_BEAR, "d46d23f04846369fa2a3702c10f759101af7bfe8419966429533cd81a11d2b", 30, MONEY_LOW));
        animals.add(build("rabbit", type, EntityType.RABBIT, "ffecc6b5e6ea5ced74c46e7627be3f0826327fba26386c6cc7863372e9bc", 30, MONEY_LOW));
        animals.add(build("wolf", type, EntityType.WOLF, "28d408842e76a5a454dc1c7e9ac5c1a8ac3f4ad34d6973b5275491dff8c5c251", 30, MONEY_LOW));
        animals.add(build("bat", type, EntityType.BAT, "3820a10db222f69ac2215d7d10dca47eeafa215553764a2b81bafd479e7933d1", 30, MONEY_LOW));
        animals.add(build("armadillo", type, EntityType.ARMADILLO, "9852b33ba294f560090752d113fe728cbc7dd042029a38d5382d65a2146068b7", 45, MONEY_LOW));

        raiders.add(build("ravager", type, EntityType.RAVAGER, "cd20bf52ec390a0799299184fc678bf84cf732bb1bd78fd1c4b441858f0235a8", 100, MONEY_HIGH));
        raiders.add(build("illusioner", type, EntityType.ILLUSIONER, "512512e7d016a2343a7bff1a4cd15357ab851579f1389bd4e3a24cbeb88b", 75, MONEY_MEDIUM));
        raiders.add(build("evoker", type, EntityType.EVOKER, "806ac02fd9dac966b7e5806736b6feb90e2f3b0577969e673291b8307c1ef8e5", 75, MONEY_MEDIUM));
        raiders.add(build("pillager", type, EntityType.PILLAGER, "18e57841607f449e76b7c820fcbd1913ec1b80c4ac81728874db230f5df2b3b", 75, MONEY_MEDIUM));
        raiders.add(build("vindicator", type, EntityType.VINDICATOR, "6deaec344ab095b48cead7527f7dee61b063ff791f76a8fa76642c8676e2173", 75, MONEY_MEDIUM));
        raiders.add(build("witch", type, EntityType.WITCH, "8aa986a6e1c2d88ff198ab2c3259e8d2674cb83a6d206f883bad2c8ada819", 75, MONEY_MEDIUM));

        skeletons.add(build("skeleton", type, EntityType.SKELETON, new NightItem(Material.SKELETON_SKULL), 60, MONEY_MEDIUM));
        skeletons.add(build("stray", type, EntityType.STRAY, "9e391c6e535f7aa5a2b6ee6d137f59f2d7c60def88853ba611ceb2d16a7e7c73", 60, MONEY_MEDIUM));
        skeletons.add(build("wither_skeleton", type, EntityType.WITHER_SKELETON, new NightItem(Material.WITHER_SKELETON_SKULL), 70, MONEY_MEDIUM));
        skeletons.add(build("skeleton_horse", type, EntityType.SKELETON_HORSE, "47effce35132c86ff72bcae77dfbb1d22587e94df3cbc2570ed17cf8973a", 35, MONEY_MEDIUM));
        skeletons.add(build("bogged", type, EntityType.BOGGED, "a3b9003ba2d05562c75119b8a62185c67130e9282f7acbac4bc2824c21eb95d9", 65, MONEY_MEDIUM));

        zombies.add(build("zombie", type, EntityType.ZOMBIE, new NightItem(Material.ZOMBIE_HEAD), 50, MONEY_LOW));
        zombies.add(build("zombie_villager", type, EntityType.ZOMBIE_VILLAGER, "b2b393be2dc2973d41a834e19dd6b73b866782d684a097ebfe99cb390194f", 60, MONEY_LOW));
        zombies.add(build("zombie_horse", type, EntityType.ZOMBIE_HORSE, "d22950f2d3efddb18de86f8f55ac518dce73f12a6e0f8636d551d8eb480ceec", 35, MONEY_LOW));
        zombies.add(build("zombified_piglin", type, EntityType.ZOMBIFIED_PIGLIN, "7eabaecc5fae5a8a49c8863ff4831aaa284198f1a2398890c765e0a8de18da8c", 80, MONEY_MEDIUM));
        zombies.add(build("husk", type, EntityType.HUSK, "d674c63c8db5f4ca628d69a3b1f8a36e29d8fd775e1a6bdb6cabb4be4db121", 50, MONEY_LOW));
        zombies.add(build("drowned", type, EntityType.DROWNED, "c84df79c49104b198cdad6d99fd0d0bcf1531c92d4ab6269e40b7d3cbbb8e98c", 70, MONEY_LOW_MEDIUM));

        fishes.add(build("cod", type, EntityType.COD, "7892d7dd6aadf35f86da27fb63da4edda211df96d2829f691462a4fb1cab0", 25, MONEY_LOW));
        fishes.add(build("salmon", type, EntityType.SALMON, "8aeb21a25e46806ce8537fbd6668281cf176ceafe95af90e94a5fd84924878", 25, MONEY_LOW));
        fishes.add(build("pufferfish", type, EntityType.PUFFERFISH, "6df8c316962949ba3be445c94ebf714108252d46459b66110f4bc14e0e1b59dc", 25, MONEY_LOW));
        fishes.add(build("squid", type, EntityType.SQUID, "d8705624daa2956aa45956c81bab5f4fdb2c74a596051e24192039aea3a8b8", 40, MONEY_LOW));
        fishes.add(build("glow_squid", type, EntityType.GLOW_SQUID, "d8705624daa2956aa45956c81bab5f4fdb2c74a596051e24192039aea3a8b8", 50, MONEY_LOW));
        fishes.add(build("tropical_fish", type, EntityType.TROPICAL_FISH, "d6dd5e6addb56acbc694ea4ba5923b1b25688178feffa72290299e2505c97281", 25, MONEY_LOW));
        fishes.add(build("axolotl", type, EntityType.AXOLOTL, "5c167410409336acc58e6433ffa8b7f86a8786e35ec7300b9062340281d4691c", 25, MONEY_LOW));
        fishes.add(build("guardian", type, EntityType.GUARDIAN, "a0bf34a71e7715b6ba52d5dd1bae5cb85f773dc9b0d457b4bfc5f9dd3cc7c94", 50, MONEY_HIGH));

        various.add(build("blaze", type, EntityType.BLAZE, "b20657e24b56e1b2f8fc219da1de788c0c24f36388b1a409d0cd2d8dba44aa3b", 70, MONEY_MEDIUM_HIGH));
        various.add(build("ghast", type, EntityType.GHAST, "64ab8a22e7687cc4c78f3b6ff5b1eb04917b51cd3cd7dbce36171160b3c77ced", 80, MONEY_MEDIUM_HIGH));
        various.add(build("magma_cube", type, EntityType.MAGMA_CUBE, "38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429", 66, MONEY_MEDIUM_HIGH));
        various.add(build("slime", type, EntityType.SLIME, "a5acd8b24f7389a40404348f4344eec2235d4ca718453be9803b60b71a125891", 44, MONEY_LOW_MEDIUM));
        various.add(build("endermite", type, EntityType.ENDERMITE, "1730127e3ac7677122422df0028d9e7368bd157738c8c3cddecc502e896be01c", 35, MONEY_LOW));
        various.add(build("enderman", type, EntityType.ENDERMAN, "c09f1de6135f4bea781c5a8e0d61095f833ee2685d8154ecea814ee6d328a5c6", 70, MONEY_MEDIUM_HIGH));
        various.add(build("piglin_brute", type, EntityType.PIGLIN_BRUTE, new NightItem(Material.PIGLIN_HEAD), 80, MONEY_MEDIUM_HIGH));
        various.add(build("hoglin", type, EntityType.HOGLIN, "9bb9bc0f01dbd762a08d9e77c08069ed7c95364aa30ca1072208561b730e8d75", 50, MONEY_MEDIUM_HIGH));
        various.add(build("zoglin", type, EntityType.ZOGLIN, "e67e18602e03035ad68967ce090235d8996663fb9ea47578d3a7ebbc42a5ccf9", 50, MONEY_MEDIUM_HIGH));
        various.add(build("cave_spider", type, EntityType.CAVE_SPIDER, "eccc4a32d45d74e8b14ef1ffd55cd5f381a06d4999081d52eaea12e13293e209", 45, MONEY_MEDIUM));
        various.add(build("spider", type, EntityType.SPIDER, "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1", 45, MONEY_LOW_MEDIUM));
        various.add(build("creeper", type, EntityType.CREEPER, new NightItem(Material.CREEPER_HEAD), 90, MONEY_MEDIUM));
        various.add(build("phantom", type, EntityType.PHANTOM, "7e95153ec23284b283f00d19d29756f244313a061b70ac03b97d236ee57bd982", 77, MONEY_MEDIUM_HIGH));
        various.add(build("vex", type, EntityType.VEX, "5e7330c7d5cd8a0a55ab9e95321535ac7ae30fe837c37ea9e53bea7ba2de86b", 66, MONEY_MEDIUM));
        various.add(build("silverfish", type, EntityType.SILVERFISH, "da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540", 33, MONEY_LOW));
        various.add(build("breeze", type, EntityType.BREEZE, "a275728af7e6a29c88125b675a39d88ae9919bb61fdc200337fed6ab0c49d65c", 55, MONEY_MEDIUM_HIGH));

        bosses.add(build("elder_guardian", type, EntityType.ELDER_GUARDIAN, "1c797482a14bfcb877257cb2cff1b6e6a8b8413336ffb4c29a6139278b436b", 100, MONEY_BOSS));
        bosses.add(build("ender_dragon", type, EntityType.ENDER_DRAGON, new NightItem(Material.DRAGON_HEAD), 100, MONEY_BOSS));
        bosses.add(build("wither", type, EntityType.WITHER, "74f328f5044129b5d1f96affd1b8c05bcde6bd8e756aff5c5020585eef8a3daf", 100, MONEY_BOSS));

        writeConfig("mobs_penalty", penalty);
        writeConfig("mobs_animals", animals);
        writeConfig("mobs_fish", fishes);
        writeConfig("mobs_raiders", raiders);
        writeConfig("mobs_skeletons", skeletons);
        writeConfig("mobs_zombies", zombies);
        writeConfig("mobs_various", various);
        writeConfig("mobs_boss", bosses);
    }

    private void createForShearing() {
        var type = LootActions.SHEARING;

        Set<MoneyObjective> entities = new HashSet<>();

        entities.add(build("sheep", type, EntityType.SHEEP, "a723893df4cfb9c7240fc47b560ccf6ddeb19da9183d33083f2c71f46dad290a", 100, MONEY_MEDIUM));
        entities.add(build("mushroom_cow", type, EntityType.MOOSHROOM, "45603d539f666fdf0f7a0fe20b81dfef3abe6c51da34b9525a5348432c5523b2", 100, MONEY_MEDIUM));
        entities.add(build("snowman", type, EntityType.SNOW_GOLEM, "8e8d206f61e6de8a79d0cb0bcd98aced464cbfefc921b4160a25282163112a", 100, MONEY_MEDIUM));

        writeConfig("shearing_all", entities);
    }

    private void writeConfig(@NotNull String filename, @NotNull Set<MoneyObjective> objectives) {
        FileConfig config = FileConfig.loadOrExtract(plugin, Config.DIR_OBJECTIVES, filename + ".yml");
        objectives.forEach(objective -> {
            objective.write(config, objective.getId());
        });
        config.saveChanges();
    }

    @NotNull
    private <O extends Keyed> MoneyObjective build(@NotNull String id,
                                                   @NotNull LootAction<?, O> type,
                                                   @NotNull Tag<O> tag,
                                                   @NotNull NightItem icon,
                                                   double chance,
                                                   @NotNull UniDouble money) {
        return this.build(id, type, tag.getValues(), icon, chance, money);
    }

    @NotNull
    private <O> MoneyObjective build(@NotNull String id,
                                     @NotNull LootAction<?, O> type,
                                     @NotNull O item,
                                     @NotNull String texture,
                                     double chance,
                                     @NotNull UniDouble money) {
        return this.build(id, type, item, NightItem.asCustomHead(texture), chance, money);
    }

    @NotNull
    private <O> MoneyObjective build(@NotNull String id,
                                     @NotNull LootAction<?, O> type,
                                     @NotNull O item,
                                     @NotNull NightItem icon,
                                     double chance,
                                     @NotNull UniDouble money) {
        return this.build(id, type, Lists.newSet(item), icon, chance, money);
    }

    @NotNull
    private <O> MoneyObjective build(@NotNull String id,
                                     @NotNull LootAction<?, O> type,
                                     @NotNull Set<O> items,
                                     @NotNull NightItem icon,
                                     double chance,
                                     @NotNull UniDouble amount) {
        Set<String> objects = new HashSet<>();

        Map<String, DropInfo> currencyDrops = new HashMap<>();
        currencyDrops.put(CurrencyId.VAULT, new DropInfo(chance, amount, UniInt.of(1, 3), "null"));

        for (O material : items) {
            objects.add(type.getObjectName(material));
        }

        return new MoneyObjective(id, type.getId(), type.getDefaultCategory(), StringUtil.capitalizeUnderscored(id), icon, objects, currencyDrops);
    }
}
