package su.nightexpress.lootconomy.money;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.action.ActionType;
import su.nightexpress.lootconomy.action.ActionTypes;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.currency.handler.VaultEconomyHandler;
import su.nightexpress.lootconomy.money.object.DropInfo;
import su.nightexpress.lootconomy.money.object.MoneyObjective;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.wrapper.UniDouble;
import su.nightexpress.nightcore.util.wrapper.UniInt;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Creator {

    private static final UniDouble MONEY_LOWEST      = UniDouble.of(0.5, 1.5);
    private static final UniDouble MONEY_LOW         = UniDouble.of(2.5, 5);
    private static final UniDouble MONEY_LOW_MEDIUM  = UniDouble.of(5, 10);
    private static final UniDouble MONEY_MEDIUM      = UniDouble.of(7, 15);
    private static final UniDouble MONEY_MEDIUM_HIGH = UniDouble.of(15, 30);
    private static final UniDouble MONEY_HIGH        = UniDouble.of(20, 50);
    private static final UniDouble MONEY_HIGHEST     = UniDouble.of(100, 500);
    private static final UniDouble MONEY_BOSS        = UniDouble.of(5000, 25000);

    private static final UniDouble MONEY_PENALTY_MEDIUM  = UniDouble.of(-7, -15);

    private final LootConomyPlugin plugin;

    public Creator(@NotNull LootConomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void create() {
        File dir = new File(this.plugin.getDataFolder(), Config.DIR_OBJECTIVES);
        if (dir.exists()) return;

        dir.mkdirs();

        this.createMiningObjectives();
        this.createExcavationObjectives();
        this.createWoodcuttingObjectives();
        this.createHarvestObjectives();
        this.createFishingObjectives();
        this.createKillEntityObjectives();
        this.createEntityShearObjectives();
    }

    private void createMiningObjectives() {
        ActionType<BlockBreakEvent, Material> type = ActionTypes.BLOCK_BREAK;

        Set<MoneyObjective> stones = Lists.newSet(
            createObjective("stones", type, List.of(Tag.BASE_STONE_OVERWORLD), new ItemStack(Material.STONE), 0.3, MONEY_LOWEST),
            createObjective("nether_stones", type, List.of(Tag.BASE_STONE_NETHER), new ItemStack(Material.NETHERRACK), 0.1, MONEY_LOWEST),
            createObjective("nylium", type, List.of(Tag.NYLIUM), new ItemStack(Material.CRIMSON_NYLIUM), 0.2, MONEY_LOWEST),
            createObjective("terracotta", type, List.of(Tag.TERRACOTTA), new ItemStack(Material.TERRACOTTA), 0.5, MONEY_LOWEST)
        );
        generateObjectives("mining_stones", stones);

        Set<MoneyObjective> ores = Lists.newSet(
            createObjective("coal_ores", type, List.of(Tag.COAL_ORES), new ItemStack(Material.COAL_ORE), 35, MONEY_LOW),
            createObjective("copper_ores", type, List.of(Tag.COPPER_ORES), new ItemStack(Material.COPPER_ORE), 45, MONEY_LOW_MEDIUM),
            createObjective("diamond_ores", type, List.of(Tag.DIAMOND_ORES), new ItemStack(Material.DIAMOND_ORE), 80, MONEY_HIGH),
            createObjective("emerald_ores", type, List.of(Tag.EMERALD_ORES), new ItemStack(Material.EMERALD_ORE), 100, MONEY_HIGHEST),
            createObjective("redstone_ores", type, List.of(Tag.REDSTONE_ORES), new ItemStack(Material.REDSTONE_ORE), 50, MONEY_MEDIUM),
            createObjective("lapis_ores", type, List.of(Tag.LAPIS_ORES), new ItemStack(Material.LAPIS_ORE), 50, MONEY_MEDIUM),
            createObjective("iron_ores", type, List.of(Tag.IRON_ORES), new ItemStack(Material.IRON_ORE), 40, MONEY_LOW_MEDIUM),
            createObjective("nether_quartz_ore", type, Material.NETHER_QUARTZ_ORE, 75, MONEY_LOW_MEDIUM),
            createObjective("nether_gold_ore", type, Material.NETHER_GOLD_ORE, 100, MONEY_MEDIUM)
        );
        generateObjectives("mining_ores", ores);
    }

    private void createExcavationObjectives() {
        ActionType<BlockBreakEvent, Material> type = ActionTypes.BLOCK_BREAK;

        Set<MoneyObjective> objectives = Lists.newSet(
            createObjective("dirt", type, List.of(Tag.DIRT), new ItemStack(Material.DIRT), 0.1, MONEY_LOWEST),
            createObjective("sand", type, List.of(Tag.SAND), new ItemStack(Material.SAND), 0.1, MONEY_LOWEST),
            createObjective("snow", type, List.of(Tag.SNOW), new ItemStack(Material.SNOW_BLOCK), 0.1, MONEY_LOWEST),
            createObjective("clay", type, Material.CLAY, 5, MONEY_LOW),
            createObjective("gravel", type, Material.GRAVEL, 5, MONEY_LOW),
            createObjective("soul_sand", type, Material.SOUL_SAND, 1, MONEY_LOW),
            createObjective("mycelium", type, Material.MYCELIUM, 0.2, MONEY_LOW)
        );
        generateObjectives("excavation", objectives);
    }

    private void createWoodcuttingObjectives() {
        ActionType<BlockBreakEvent, Material> type = ActionTypes.BLOCK_BREAK;

        Set<MoneyObjective> berries = Lists.newSet(
            createObjective("acacia_logs", type, List.of(Tag.ACACIA_LOGS), new ItemStack(Material.ACACIA_LOG), 10, MONEY_LOW),
            createObjective("birch_logs", type, List.of(Tag.BIRCH_LOGS), new ItemStack(Material.BIRCH_LOG), 15, MONEY_LOW),
            createObjective("crimson_stems", type, List.of(Tag.CRIMSON_STEMS), new ItemStack(Material.CRIMSON_STEM), 15, MONEY_LOW),
            createObjective("dark_oak_logs", type, List.of(Tag.DARK_OAK_LOGS), new ItemStack(Material.DARK_OAK_LOG), 15, MONEY_LOW),
            createObjective("jungle_logs", type, List.of(Tag.JUNGLE_LOGS), new ItemStack(Material.JUNGLE_LOG), 20, MONEY_LOW),
            createObjective("mangrove_logs", type, List.of(Tag.MANGROVE_LOGS), new ItemStack(Material.MANGROVE_LOG), 15, MONEY_LOW),
            createObjective("oak_logs", type, List.of(Tag.OAK_LOGS), new ItemStack(Material.OAK_LOG), 10, MONEY_LOW),
            createObjective("spruce_logs", type, List.of(Tag.SPRUCE_LOGS), new ItemStack(Material.SPRUCE_LOG), 10, MONEY_LOW),
            createObjective("warped_stems", type, List.of(Tag.WARPED_STEMS), new ItemStack(Material.WARPED_STEM), 15, MONEY_LOW),
            createObjective("leaves", type, List.of(Tag.LEAVES), new ItemStack(Material.OAK_LEAVES), 3, MONEY_LOWEST)
        );
        if (Version.isAtLeast(Version.V1_20_R2)) {
            berries.add(createObjective("cherry_logs", type, List.of(Tag.CHERRY_LOGS), new ItemStack(Material.CHERRY_LOG), 15, MONEY_LOW));
        }

        generateObjectives("woodcutting", berries);
    }

    private void createHarvestObjectives() {
        ActionType<BlockBreakEvent, Material> type = ActionTypes.BLOCK_BREAK;
        ActionType<PlayerHarvestBlockEvent, Material> harvest = ActionTypes.BLOCK_HARVEST;

        Set<MoneyObjective> crops = Lists.newSet(
            createObjective("pumpkin", type, Material.PUMPKIN, 100, MONEY_LOW),
            createObjective("melon", type, Material.MELON, 100, MONEY_LOW),
            createObjective("beetroots", type, Material.BEETROOTS, new ItemStack(Material.BEETROOT), 100, MONEY_LOW),
            createObjective("carrots", type, Material.CARROTS, new ItemStack(Material.CARROT), 100, MONEY_LOW),
            createObjective("potatoes", type, Material.POTATOES, new ItemStack(Material.POTATO), 100, MONEY_LOW),
            createObjective("wheat", type, Material.WHEAT, 100, MONEY_LOW),
            createObjective("nether_wart", type, Material.NETHER_WART, 100, MONEY_LOW),
            createObjective("red_mushroom", type, Material.RED_MUSHROOM, 100, MONEY_LOW),
            createObjective("brown_mushroom", type, Material.BROWN_MUSHROOM, 100, MONEY_LOW),
            createObjective("sugar_cane", type, Material.SUGAR_CANE, 100, MONEY_LOW),
            createObjective("bamboo", type, Material.BAMBOO, 100, MONEY_LOW)
        );
        generateObjectives("farming_crops", crops);

        Set<MoneyObjective> berries = Lists.newSet(
            createObjective("sweet_berries", harvest, Material.SWEET_BERRIES, 100, MONEY_LOW),
            createObjective("glow_berries", harvest, Material.GLOW_BERRIES, 100, MONEY_LOW)
        );
        generateObjectives("farming_berries", berries);
    }

    private void createFishingObjectives() {
        ActionType<PlayerFishEvent, Material> type = ActionTypes.ITEM_FISH;

        Set<MoneyObjective> fishes = Lists.newSet(
            createObjective("cod", type, Material.COD, 100, MONEY_LOW),
            createObjective("salmon", type, Material.SALMON, 100, MONEY_LOW_MEDIUM),
            createObjective("tropical_fish", type, Material.TROPICAL_FISH, 100, MONEY_LOW_MEDIUM),
            createObjective("pufferfish", type, Material.PUFFERFISH, 100, MONEY_LOW_MEDIUM)
        );
        generateObjectives("fishing_fishes", fishes);


        Set<MoneyObjective> treasures = Lists.newSet(
            createObjective("bow", type, Material.BOW, 100, MONEY_MEDIUM),
            createObjective("enchanted_book", type, Material.ENCHANTED_BOOK, 100, MONEY_HIGH),
            createObjective("fishing_rod", type, Material.FISHING_ROD, 100, MONEY_LOW_MEDIUM),
            createObjective("name_tag", type, Material.NAME_TAG, 100, MONEY_MEDIUM),
            createObjective("nautilus_shell", type, Material.NAUTILUS_SHELL, 100, MONEY_MEDIUM),
            createObjective("saddle", type, Material.SADDLE, 100, MONEY_MEDIUM)
        );
        generateObjectives("fishing_treasures", treasures);


        Set<MoneyObjective> junk = Lists.newSet(
            createObjective("lily_pad", type, Material.LILY_PAD, 100, MONEY_LOW),
            createObjective("bowl", type, Material.BOWL, 100, MONEY_LOW),
            createObjective("leather", type, Material.LEATHER, 100, MONEY_LOW),
            createObjective("leather_boots", type, Material.LEATHER_BOOTS, 100, MONEY_LOW),
            createObjective("rotten_flesh", type, Material.ROTTEN_FLESH, 100, MONEY_LOW),
            createObjective("stick", type, Material.STICK, 100, MONEY_LOW),
            createObjective("string", type, Material.STRING, 100, MONEY_LOW),
            createObjective("potion", type, Material.POTION, 100, MONEY_LOW),
            createObjective("bone", type, Material.BONE, 100, MONEY_LOW),
            createObjective("ink_sac", type, Material.INK_SAC, 100, MONEY_LOW),
            createObjective("tripwire_hook", type, Material.TRIPWIRE_HOOK, 100, MONEY_LOW)
        );
        generateObjectives("fishing_junk", junk);
    }

    private void createKillEntityObjectives() {
        ActionType<EntityDeathEvent, EntityType> type = ActionTypes.ENTITY_KILL;

        Set<MoneyObjective> penalty = Lists.newSet(
            createObjective("dolphin", type, EntityType.DOLPHIN, "8e9688b950d880b55b7aa2cfcd76e5a0fa94aac6d16f78e833f7443ea29fed3", 100, MONEY_PENALTY_MEDIUM),
            createObjective("bee", type, EntityType.BEE, "cce9edbbc5fdc0d8487ac72eab239d2cacfe408d74288d6384b044111ba4de0f", 100, MONEY_PENALTY_MEDIUM),
            createObjective("ocelot", type, EntityType.OCELOT, "5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1", 100, MONEY_PENALTY_MEDIUM),
            createObjective("horse", type, EntityType.HORSE, "a996399fff9cbcfb7ba677dd0c2d104229d1cc2307a6f075a882da4694ef80ae", 100, MONEY_PENALTY_MEDIUM),
            createObjective("mule", type, EntityType.MULE, "a0486a742e7dda0bae61ce2f55fa13527f1c3b334c57c034bb4cf132fb5f5f", 100, MONEY_PENALTY_MEDIUM),
            createObjective("donkey", type, EntityType.DONKEY, "63a976c047f412ebc5cb197131ebef30c004c0faf49d8dd4105fca1207edaff3", 100, MONEY_PENALTY_MEDIUM),
            createObjective("villager", type, EntityType.VILLAGER, "a36e9841794a37eb99524925668b47a62b5cb72e096a9f8f95e106804ae13e1b", 100, MONEY_PENALTY_MEDIUM),
            createObjective("wandering_trader", type, EntityType.WANDERING_TRADER, "ee011aac817259f2b48da3e5ef266094703866608b3d7d1754432bf249cd2234", 100, MONEY_PENALTY_MEDIUM),
            createObjective("turtle", type, EntityType.TURTLE, "8fa552139966c5fac1b98061ce23fc0ddef058c163142dd6d1c768cd2da207c2", 100, MONEY_PENALTY_MEDIUM),
            createObjective("cat", type, EntityType.CAT, "d0dba942c06b77a2828e3f66a1faec5e8643e9ea61a81a4523279739ed82d", 100, MONEY_PENALTY_MEDIUM),
            createObjective("llama", type, EntityType.LLAMA, "9f7d90b305aa64313c8d4404d8d652a96eba8a754b67f4347dcccdd5a6a63398", 100, MONEY_PENALTY_MEDIUM),
            createObjective("trader_llama", type, EntityType.TRADER_LLAMA, "8424780b3c5c5351cf49fb5bf41fcb289491df6c430683c84d7846188db4f84d", 100, MONEY_PENALTY_MEDIUM),
            createObjective("iron_golem", type, EntityType.IRON_GOLEM, "a9ceb73d97cf5dc32e333dbef7af25f39e42033d684649075ba4681af2a3c01b", 100, MONEY_PENALTY_MEDIUM),
            createObjective("snowman", type, EntityType.SNOWMAN, "8e8d206f61e6de8a79d0cb0bcd98aced464cbfefc921b4160a25282163112a", 100, MONEY_PENALTY_MEDIUM),
            createObjective("sniffer", type, EntityType.SNIFFER, "fe5a8341c478a134302981e6a7758ea4ecfd8d62a0df4067897e75502f9b25de", 100, MONEY_PENALTY_MEDIUM)

        );
        if (Version.isAtLeast(Version.V1_20_R3)) {
            penalty.add(createObjective("camel", type, EntityType.CAMEL, "ba4c95bfa0b61722255389141b505cf1a38bad9b0ef543de619f0cc9221ed974", 100, MONEY_PENALTY_MEDIUM));
        }
        generateObjectives("kill_entity_penalty", penalty);


        Set<MoneyObjective> raiders = Lists.newSet(
            createObjective("ravager", type, EntityType.RAVAGER, "cd20bf52ec390a0799299184fc678bf84cf732bb1bd78fd1c4b441858f0235a8", 100, MONEY_HIGH),
            createObjective("illusioner", type, EntityType.ILLUSIONER, "512512e7d016a2343a7bff1a4cd15357ab851579f1389bd4e3a24cbeb88b", 75, MONEY_MEDIUM),
            createObjective("evoker", type, EntityType.EVOKER, "806ac02fd9dac966b7e5806736b6feb90e2f3b0577969e673291b8307c1ef8e5", 75, MONEY_MEDIUM),
            createObjective("pillager", type, EntityType.PILLAGER, "18e57841607f449e76b7c820fcbd1913ec1b80c4ac81728874db230f5df2b3b", 75, MONEY_MEDIUM),
            createObjective("vindicator", type, EntityType.VINDICATOR, "6deaec344ab095b48cead7527f7dee61b063ff791f76a8fa76642c8676e2173", 75, MONEY_MEDIUM),
            createObjective("witch", type, EntityType.WITCH, "8aa986a6e1c2d88ff198ab2c3259e8d2674cb83a6d206f883bad2c8ada819", 75, MONEY_MEDIUM)
        );
        generateObjectives("kill_entity_raiders", raiders);


        Set<MoneyObjective> skeletons = Lists.newSet(
            createObjective("skeleton", type, EntityType.SKELETON, new ItemStack(Material.SKELETON_SKULL), 60, MONEY_MEDIUM),
            createObjective("stray", type, EntityType.STRAY, "9e391c6e535f7aa5a2b6ee6d137f59f2d7c60def88853ba611ceb2d16a7e7c73", 60, MONEY_MEDIUM),
            createObjective("wither_skeleton", type, EntityType.WITHER_SKELETON, new ItemStack(Material.WITHER_SKELETON_SKULL), 70, MONEY_MEDIUM),
            createObjective("skeleton_horse", type, EntityType.SKELETON_HORSE, "47effce35132c86ff72bcae77dfbb1d22587e94df3cbc2570ed17cf8973a", 35, MONEY_MEDIUM)
        );
        if (Version.isAtLeast(Version.MC_1_21)) {
            skeletons.add(createObjective("bogged", type, EntityType.valueOf("BOGGED"), "a3b9003ba2d05562c75119b8a62185c67130e9282f7acbac4bc2824c21eb95d9", 65, MONEY_MEDIUM));
        }
        generateObjectives("kill_entity_skeletons", skeletons);


        Set<MoneyObjective> animals = Lists.newSet(
            createObjective("pig", type, EntityType.PIG, "fa305e321e87ec91421ecccf7cfef10703fb77f62658d6b998f117fcf34cd0b2", 30, MONEY_LOW),
            createObjective("cow", type, EntityType.COW, "b667c0e107be79d7679bfe89bbc57c6bf198ecb529a3295fcfdfd2f24408dca3", 30, MONEY_LOW),
            createObjective("sheep", type, EntityType.SHEEP, "a723893df4cfb9c7240fc47b560ccf6ddeb19da9183d33083f2c71f46dad290a", 30, MONEY_LOW),
            createObjective("goat", type, EntityType.GOAT, "457a0d538fa08a7affe312903468861720f9fa34e86d44b89dcec5639265f03", 30, MONEY_LOW),
            createObjective("chicken", type, EntityType.CHICKEN, "1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893", 30, MONEY_LOW),
            createObjective("mushroom_cow", type, EntityType.MUSHROOM_COW, "45603d539f666fdf0f7a0fe20b81dfef3abe6c51da34b9525a5348432c5523b2", 30, MONEY_LOW),
            createObjective("fox", type, EntityType.FOX, "d8954a42e69e0881ae6d24d4281459c144a0d5a968aed35d6d3d73a3c65d26a", 30, MONEY_LOW),
            createObjective("frog", type, EntityType.FROG, "45852a95928897746012988fbd5dbaa1b70b7a5fb65157016f4ff3f245374c08", 30, MONEY_LOW),
            createObjective("panda", type, EntityType.PANDA, "8018a1771d69c11b8dad42cd310375ba2d827932b25ef357f7e572c1bd0f9", 30, MONEY_LOW),
            createObjective("parrot", type, EntityType.PARROT, "a4ba8d66fecb1992e94b8687d6ab4a5320ab7594ac194a2615ed4df818edbc3", 30, MONEY_LOW),
            createObjective("polar_bear", type, EntityType.POLAR_BEAR, "d46d23f04846369fa2a3702c10f759101af7bfe8419966429533cd81a11d2b", 30, MONEY_LOW),
            createObjective("rabbit", type, EntityType.RABBIT, "ffecc6b5e6ea5ced74c46e7627be3f0826327fba26386c6cc7863372e9bc", 30, MONEY_LOW),
            createObjective("wolf", type, EntityType.WOLF, "28d408842e76a5a454dc1c7e9ac5c1a8ac3f4ad34d6973b5275491dff8c5c251", 30, MONEY_LOW),
            createObjective("bat", type, EntityType.BAT, "3820a10db222f69ac2215d7d10dca47eeafa215553764a2b81bafd479e7933d1", 30, MONEY_LOW)
        );
        if (Version.isAtLeast(Version.MC_1_21)) {
            animals.add(createObjective("armadillo", type, EntityType.valueOf("ARMADILLO"), "9852b33ba294f560090752d113fe728cbc7dd042029a38d5382d65a2146068b7", 45, MONEY_LOW));
        }
        generateObjectives("kill_entity_animals", animals);


        Set<MoneyObjective> zombies = Lists.newSet(
            createObjective("zombie", type, EntityType.ZOMBIE, new ItemStack(Material.ZOMBIE_HEAD), 50, MONEY_LOW),
            createObjective("zombie_villager", type, EntityType.ZOMBIE_VILLAGER, "b2b393be2dc2973d41a834e19dd6b73b866782d684a097ebfe99cb390194f", 60, MONEY_LOW),
            createObjective("zombie_horse", type, EntityType.ZOMBIE_HORSE, "d22950f2d3efddb18de86f8f55ac518dce73f12a6e0f8636d551d8eb480ceec", 35, MONEY_LOW),
            createObjective("zombified_piglin", type, EntityType.ZOMBIFIED_PIGLIN, "7eabaecc5fae5a8a49c8863ff4831aaa284198f1a2398890c765e0a8de18da8c", 80, MONEY_MEDIUM),
            createObjective("husk", type, EntityType.HUSK, "d674c63c8db5f4ca628d69a3b1f8a36e29d8fd775e1a6bdb6cabb4be4db121", 50, MONEY_LOW),
            createObjective("drowned", type, EntityType.DROWNED, "c84df79c49104b198cdad6d99fd0d0bcf1531c92d4ab6269e40b7d3cbbb8e98c", 70, MONEY_LOW_MEDIUM)
        );
        generateObjectives("kill_entity_zombies", zombies);


        Set<MoneyObjective> fishes = Lists.newSet(
            createObjective("cod", type, EntityType.COD, "7892d7dd6aadf35f86da27fb63da4edda211df96d2829f691462a4fb1cab0", 25, MONEY_LOW),
            createObjective("salmon", type, EntityType.SALMON, "8aeb21a25e46806ce8537fbd6668281cf176ceafe95af90e94a5fd84924878", 25, MONEY_LOW),
            createObjective("pufferfish", type, EntityType.PUFFERFISH, "6df8c316962949ba3be445c94ebf714108252d46459b66110f4bc14e0e1b59dc", 25, MONEY_LOW),
            createObjective("squid", type, EntityType.SQUID, "d8705624daa2956aa45956c81bab5f4fdb2c74a596051e24192039aea3a8b8", 40, MONEY_LOW),
            createObjective("glow_squid", type, EntityType.GLOW_SQUID, "d8705624daa2956aa45956c81bab5f4fdb2c74a596051e24192039aea3a8b8", 50, MONEY_LOW),
            createObjective("tropical_fish", type, EntityType.TROPICAL_FISH, "d6dd5e6addb56acbc694ea4ba5923b1b25688178feffa72290299e2505c97281", 25, MONEY_LOW),
            createObjective("axolotl", type, EntityType.AXOLOTL, "5c167410409336acc58e6433ffa8b7f86a8786e35ec7300b9062340281d4691c", 25, MONEY_LOW),
            createObjective("guardian", type, EntityType.GUARDIAN, "a0bf34a71e7715b6ba52d5dd1bae5cb85f773dc9b0d457b4bfc5f9dd3cc7c94", 50, MONEY_HIGH)
        );
        generateObjectives("kill_entity_fish", fishes);


        Set<MoneyObjective> various = Lists.newSet(
            createObjective("blaze", type, EntityType.BLAZE, "b20657e24b56e1b2f8fc219da1de788c0c24f36388b1a409d0cd2d8dba44aa3b", 70, MONEY_MEDIUM_HIGH),
            createObjective("ghast", type, EntityType.GHAST, "64ab8a22e7687cc4c78f3b6ff5b1eb04917b51cd3cd7dbce36171160b3c77ced", 80, MONEY_MEDIUM_HIGH),
            createObjective("magma_cube", type, EntityType.MAGMA_CUBE, "38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429", 66, MONEY_MEDIUM_HIGH),
            createObjective("slime", type, EntityType.SLIME, "a5acd8b24f7389a40404348f4344eec2235d4ca718453be9803b60b71a125891", 44, MONEY_LOW_MEDIUM),
            createObjective("endermite", type, EntityType.ENDERMITE, "1730127e3ac7677122422df0028d9e7368bd157738c8c3cddecc502e896be01c", 35, MONEY_LOW),
            createObjective("enderman", type, EntityType.ENDERMAN, "c09f1de6135f4bea781c5a8e0d61095f833ee2685d8154ecea814ee6d328a5c6", 70, MONEY_MEDIUM_HIGH),
            createObjective("piglin_brute", type, EntityType.PIGLIN_BRUTE, new ItemStack(Material.PIGLIN_HEAD), 80, MONEY_MEDIUM_HIGH),
            createObjective("hoglin", type, EntityType.HOGLIN, "9bb9bc0f01dbd762a08d9e77c08069ed7c95364aa30ca1072208561b730e8d75", 50, MONEY_MEDIUM_HIGH),
            createObjective("zoglin", type, EntityType.ZOGLIN, "e67e18602e03035ad68967ce090235d8996663fb9ea47578d3a7ebbc42a5ccf9", 50, MONEY_MEDIUM_HIGH),
            createObjective("cave_spider", type, EntityType.CAVE_SPIDER, "eccc4a32d45d74e8b14ef1ffd55cd5f381a06d4999081d52eaea12e13293e209", 45, MONEY_MEDIUM),
            createObjective("spider", type, EntityType.SPIDER, "cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1", 45, MONEY_LOW_MEDIUM),
            createObjective("creeper", type, EntityType.CREEPER, new ItemStack(Material.CREEPER_HEAD), 90, MONEY_MEDIUM),
            createObjective("phantom", type, EntityType.PHANTOM, "7e95153ec23284b283f00d19d29756f244313a061b70ac03b97d236ee57bd982", 77, MONEY_MEDIUM_HIGH),
            createObjective("vex", type, EntityType.VEX, "5e7330c7d5cd8a0a55ab9e95321535ac7ae30fe837c37ea9e53bea7ba2de86b", 66, MONEY_MEDIUM),
            createObjective("silverfish", type, EntityType.SILVERFISH, "da91dab8391af5fda54acd2c0b18fbd819b865e1a8f1d623813fa761e924540", 33, MONEY_LOW)
        );
        if (Version.isAtLeast(Version.MC_1_21)) {
            various.add(createObjective("breeze", type, EntityType.valueOf("BREEZE"), "a275728af7e6a29c88125b675a39d88ae9919bb61fdc200337fed6ab0c49d65c", 55, MONEY_MEDIUM_HIGH));
        }
        generateObjectives("kill_entity_various", various);


        Set<MoneyObjective> bosses = Lists.newSet(
            createObjective("elder_guardian", type, EntityType.ELDER_GUARDIAN, "1c797482a14bfcb877257cb2cff1b6e6a8b8413336ffb4c29a6139278b436b", 100, MONEY_BOSS),
            createObjective("ender_dragon", type, EntityType.ENDER_DRAGON, new ItemStack(Material.DRAGON_HEAD), 100, MONEY_BOSS),
            createObjective("wither", type, EntityType.WITHER, "74f328f5044129b5d1f96affd1b8c05bcde6bd8e756aff5c5020585eef8a3daf", 100, MONEY_BOSS)
        );
        generateObjectives("kill_entity_boss", bosses);
    }

    private void createEntityShearObjectives() {
        ActionType<PlayerShearEntityEvent, EntityType> type = ActionTypes.ENTITY_SHEAR;

        Set<MoneyObjective> entities = Lists.newSet(
            createObjective("sheep", type, EntityType.SHEEP, "a723893df4cfb9c7240fc47b560ccf6ddeb19da9183d33083f2c71f46dad290a", 100, MONEY_MEDIUM),
            createObjective("mushroom_cow", type, EntityType.MUSHROOM_COW, "45603d539f666fdf0f7a0fe20b81dfef3abe6c51da34b9525a5348432c5523b2", 100, MONEY_MEDIUM),
            createObjective("snowman", type, EntityType.SNOWMAN, "8e8d206f61e6de8a79d0cb0bcd98aced464cbfefc921b4160a25282163112a", 100, MONEY_MEDIUM)
        );
        generateObjectives("shear_entity", entities);

    }

    private void generateObjectives(@NotNull String filename, @NotNull Set<MoneyObjective> objectives) {
        FileConfig config = FileConfig.loadOrExtract(plugin, Config.DIR_OBJECTIVES, filename + ".yml");
        objectives.forEach(objective -> {
            objective.write(config, objective.getId());
        });
        config.saveChanges();
    }

    @NotNull
    private <O extends Keyed> MoneyObjective createObjective(@NotNull String id,
                                                             @NotNull ActionType<?, O> type,
                                                             @NotNull List<Tag<O>> tags,
                                                             @NotNull ItemStack iconType,
                                                             double chance,
                                                             @NotNull UniDouble money) {
        Set<O> materials = tags.stream().flatMap(tag -> tag.getValues().stream()).collect(Collectors.toSet());
        return this.createObjective(id, type, materials, iconType, chance, money);
    }

    @NotNull
    private <O> MoneyObjective createObjective(@NotNull String id,
                                               @NotNull ActionType<?, O> type,
                                               @NotNull O item,
                                               @NotNull String texture,
                                               double chance,
                                               @NotNull UniDouble money) {
        return this.createObjective(id, type, item, ItemUtil.getSkinHead(texture), chance, money);
    }

    @NotNull
    private MoneyObjective createObjective(@NotNull String id,
                                           @NotNull ActionType<?, Material> type,
                                           @NotNull Material item,
                                           double chance,
                                           @NotNull UniDouble money) {
        return this.createObjective(id, type, item, new ItemStack(item), chance, money);
    }

    @NotNull
    private <O> MoneyObjective createObjective(@NotNull String id,
                                               @NotNull ActionType<?, O> type,
                                               @NotNull O item,
                                               @NotNull ItemStack icon,
                                               double chance,
                                               @NotNull UniDouble money) {
        return this.createObjective(id, type, Lists.newSet(item), icon, chance, money);
    }

    @NotNull
    private <O> MoneyObjective createObjective(@NotNull String id,
                                               @NotNull ActionType<?, O> type,
                                               @NotNull Set<O> items,
                                               @NotNull ItemStack icon,
                                               double chance,
                                               @NotNull UniDouble amount) {
        Set<String> objects = new HashSet<>();

        Map<String, DropInfo> currencyDrops = new HashMap<>();
        currencyDrops.put(VaultEconomyHandler.ID, new DropInfo(chance, amount, UniInt.of(1, 3), "null"));

        for (O material : items) {
            objects.add(type.getObjectName(material));
        }

        return new MoneyObjective(type, id, StringUtil.capitalizeUnderscored(id), icon, objects, currencyDrops);
    }
}
