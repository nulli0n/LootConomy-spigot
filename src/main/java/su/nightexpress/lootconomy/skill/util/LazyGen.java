package su.nightexpress.lootconomy.skill.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.LootConomyAPI;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.skill.impl.SkillObjective;

import java.util.*;
import java.util.stream.Collectors;

public class LazyGen {

    public static void generate() {
        generateForMining();
        generateForExcavation();
        generateWoodcutting();
        generateHarvesting();
        generateAquaMining();
        generateFishing();
        generateHunting();
    }

    public static void generateForMining() {
        String mining = "mining";

        generateObjectives(mining, "base",
            getMaterialObjectives(List.of(
            Tag.BASE_STONE_OVERWORLD,
            Tag.BASE_STONE_NETHER
        ), 5, 0.1, 0.3));

        generateObjectives(mining, "ores",
            getMaterialObjectives(List.of(Tag.COAL_ORES, Tag.COPPER_ORES), 50, 5, 10));

        generateObjectives(mining, "ores",
            getMaterialObjectives(List.of(Tag.DIAMOND_ORES), 100, 50, 150));

        generateObjectives(mining, "ores",
            getMaterialObjectives(List.of(Tag.EMERALD_ORES), 100, 150, 300));

        generateObjectives(mining, "ores",
            getMaterialObjectives(
                List.of(Tag.GOLD_ORES, Tag.REDSTONE_ORES, Tag.LAPIS_ORES), 80, 5, 15)
        );

        generateObjectives(mining, "ores",
            getMaterialObjectives(
                Set.of(
                    Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE
                ), 80, 5, 15)
        );

        generateObjectives(mining, "ores",
            getMaterialObjectives(List.of(Tag.IRON_ORES), 75, 5, 10));

        generateObjectives(mining, "nylium",
            getMaterialObjectives(List.of(Tag.NYLIUM), 6.5, 0.35, 0.5));

        generateObjectives(mining, "bricks",
            getMaterialObjectives(List.of(Tag.STONE_BRICKS), 45, 2, 6));

        generateObjectives(mining, "terracotta",
            getMaterialObjectives(List.of(Tag.TERRACOTTA), 5, 0.25, 0.45));
    }

    public static void generateForExcavation() {
        String excavation = "excavation";

        generateObjectives(excavation, "base",
            getMaterialObjectives(List.of(
                Tag.DIRT, Tag.SAND, Tag.ANIMALS_SPAWNABLE_ON, Tag.AXOLOTLS_SPAWNABLE_ON
            ), 5, 0.15, 0.25));

        generateObjectives(excavation, "base_2",
            getMaterialObjectives(Set.of(
                Material.GRAVEL, Material.CLAY, Material.SUSPICIOUS_GRAVEL, Material.SOUL_SAND
            ), 7.5, 2, 5));

        generateObjectives(excavation, "mycelium",
            getMaterialObjectives(Set.of(Material.MYCELIUM), 5, 0.2, 0.4));

        generateObjectives(excavation, "snow",
            getMaterialObjectives(List.of(Tag.SNOW), 3.5, 0.1, 0.25));
    }

    public static void generateWoodcutting() {
        String woodcutting = "woodcutting";

        generateObjectives(woodcutting, "acacia",
            getMaterialObjectives(List.of(Tag.ACACIA_LOGS), 50, 4, 8));

        generateObjectives(woodcutting, "birch",
            getMaterialObjectives(List.of(Tag.BIRCH_LOGS), 50, 3, 7));

        generateObjectives(woodcutting, "cherry",
            getMaterialObjectives(List.of(Tag.CHERRY_LOGS), 50, 3, 7));

        generateObjectives(woodcutting, "crimson",
            getMaterialObjectives(List.of(Tag.CRIMSON_STEMS), 50, 3, 7));

        generateObjectives(woodcutting, "dark_oak",
            getMaterialObjectives(List.of(Tag.DARK_OAK_LOGS), 50, 3, 7));

        generateObjectives(woodcutting, "jungle",
            getMaterialObjectives(List.of(Tag.JUNGLE_LOGS), 50, 3, 7));

        generateObjectives(woodcutting, "mangrove",
            getMaterialObjectives(List.of(Tag.MANGROVE_LOGS), 50, 3, 7));

        generateObjectives(woodcutting, "oak",
            getMaterialObjectives(List.of(Tag.OAK_LOGS), 50, 3, 7));

        generateObjectives(woodcutting, "spruce",
            getMaterialObjectives(List.of(Tag.SPRUCE_LOGS), 50, 3, 7));

        generateObjectives(woodcutting, "warped",
            getMaterialObjectives(List.of(Tag.WARPED_STEMS), 50, 3, 7));

        generateObjectives(woodcutting, "leaves",
            getMaterialObjectives(List.of(Tag.LEAVES), 25, 1, 4));
    }

    public static void generateHarvesting() {
        String harvesting = "harvesting";

        generateObjectives(harvesting, "vines",
            getMaterialObjectives(List.of(Tag.CAVE_VINES), 60, 1, 5));

        generateObjectives(harvesting, "crops",
            getMaterialObjectives(List.of(Tag.MAINTAINS_FARMLAND), 100, 4, 12));

        generateObjectives(harvesting, "crops",
            getMaterialObjectives(
                Set.of(
                    Material.MELON, Material.PUMPKIN, Material.NETHER_WART,
                    Material.SWEET_BERRIES, Material.GLOW_BERRIES
                ), 100, 4, 12));

        generateObjectives(harvesting, "flowers",
            getMaterialObjectives(List.of(Tag.FLOWERS, Tag.SMALL_FLOWERS, Tag.TALL_FLOWERS), 30, 2, 4));

        generateObjectives(harvesting, "ground_plants",
            getMaterialObjectives(Set.of(
                Material.CRIMSON_ROOTS, Material.MANGROVE_ROOTS, Material.HANGING_ROOTS,
                Material.CRIMSON_FUNGUS, Material.WARPED_FUNGUS,
                Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.SWEET_BERRY_BUSH
            ), 30, 2, 4));
    }

    public static void generateAquaMining() {
        String aquamining = "aquamining";

        generateObjectives(aquamining, "corals",
            getMaterialObjectives(List.of(Tag.CORALS, Tag.CORAL_BLOCKS, Tag.CORAL_PLANTS), 25, 3, 12));
    }

    public static void generateFishing() {
        String fishing = "fishing";

        generateObjectives(fishing, "fishes",
            getMaterialObjectives(List.of(Tag.ITEMS_FISHES), 100, 5, 15));

        generateObjectives(fishing, "treasures",
            getMaterialObjectives(
                Set.of(
                    Material.BOW, Material.ENCHANTED_BOOK, Material.FISHING_ROD,
                    Material.NAME_TAG, Material.NAUTILUS_SHELL, Material.SADDLE
                ), 100, 15, 27));

        generateObjectives(fishing, "junk",
            getMaterialObjectives(
                Set.of(
                    Material.LILY_PAD, Material.BOWL, Material.LEATHER, Material.LEATHER_BOOTS,
                    Material.ROTTEN_FLESH, Material.STICK, Material.STRING, Material.POTION,
                    Material.BONE, Material.INK_SAC, Material.TRIPWIRE_HOOK
                ), 100, 2, 8));
    }

    public static void generateHunting() {
        String hunting = "hunting";

        generateObjectives(hunting, "raiders",
            getEntityObjectives(List.of(Tag.ENTITY_TYPES_RAIDERS), 80, 7, 15));

        generateObjectives(hunting, "skeletons",
            getEntityObjectives(List.of(Tag.ENTITY_TYPES_SKELETONS), 80, 9, 18));

        generateObjectives(hunting, "animals",
            getEntityObjectives(
                Set.of(
                    EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.GOAT,
                    EntityType.CHICKEN, EntityType.HORSE, EntityType.MULE, EntityType.DONKEY,
                    EntityType.MUSHROOM_COW, EntityType.TURTLE, EntityType.CAMEL, EntityType.CAT,
                    EntityType.FOX, EntityType.FROG, EntityType.LLAMA, EntityType.OCELOT, EntityType.PANDA,
                    EntityType.PARROT, EntityType.POLAR_BEAR, EntityType.RABBIT, EntityType.TRADER_LLAMA,
                    EntityType.WOLF, EntityType.BEE, EntityType.BAT, EntityType.SILVERFISH, EntityType.SKELETON_HORSE
                ), 50, 4, 12));

        generateObjectives(hunting, "zombies",
            getEntityObjectives(
                Set.of(
                    EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER,
                    EntityType.ZOMBIE_HORSE, EntityType.ZOMBIFIED_PIGLIN,
                    EntityType.GIANT, EntityType.HUSK
                ), 50, 6, 16));

        generateObjectives(hunting, "fish",
            getEntityObjectives(
                Set.of(
                    EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH,
                    EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.TADPOLE,
                    EntityType.AXOLOTL
                ), 75, 3, 9));

        generateObjectives(hunting, "fish",
            getEntityObjectives(
                Set.of(
                    EntityType.GUARDIAN
                ), 90, 15, 30));

        generateObjectives(hunting, "other",
            getEntityObjectives(
                Set.of(EntityType.IRON_GOLEM, EntityType.SNOWMAN), 50, 5, 20));

        generateObjectives(hunting, "hostile",
            getEntityObjectives(
                Set.of(
                    EntityType.BLAZE, EntityType.GHAST, EntityType.MAGMA_CUBE, EntityType.SLIME,
                    EntityType.ENDERMITE, EntityType.ENDERMAN, EntityType.PIGLIN_BRUTE, EntityType.PIG,
                    EntityType.HOGLIN, EntityType.ZOGLIN, EntityType.CAVE_SPIDER, EntityType.SPIDER,
                    EntityType.CREEPER, EntityType.DROWNED, EntityType.PHANTOM, EntityType.VEX,
                    EntityType.SILVERFISH
                ), 50, 6, 16));

        generateObjectives(hunting, "boss",
            getEntityObjectives(
                Set.of(
                    EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, EntityType.WITHER
                ), 100, 1000, 5000));
    }

    public static void generateObjectives(@NotNull String skillId, @NotNull String conf, @NotNull Set<SkillObjective> objectives) {
        LootConomy plugin = LootConomyAPI.PLUGIN;

        JYML cfg = JYML.loadOrExtract(plugin, "/skills/" + skillId + "/objectives/", conf + ".yml");
        objectives.forEach(objective -> {
            objective.write(cfg, objective.getName());
        });
        cfg.saveChanges();
    }

    @NotNull
    public static Set<SkillObjective> getMaterialObjectives(@NotNull List<Tag<Material>> tags, double chance, double min, double max) {
        Set<Material> materials = tags.stream().flatMap(t -> t.getValues().stream()).collect(Collectors.toSet());
        return getMaterialObjectives(materials, chance, min, max);
    }

    @NotNull
    public static Set<SkillObjective> getMaterialObjectives(@NotNull Set<Material> materials, double chance, double min, double max) {
        Set<SkillObjective> objectives = new HashSet<>();
        Collection<Currency> currencies = LootConomyAPI.PLUGIN.getCurrencyManager().getCurrencies();

        for (Material material : materials) {
            String name = material.name();
            String display = LangManager.getMaterial(material);
            ItemStack icon = new ItemStack(material);
            Map<String, DropInfo> currencyDrops = new HashMap<>();
            currencies.forEach(currency -> {
                if (!currency.getId().equalsIgnoreCase(EngineUtils.VAULT)) return;
                currencyDrops.put(currency.getId(), new DropInfo(chance, min, max));
            });
            DropInfo xpDrop = new DropInfo(chance, 1, 5);

            SkillObjective objective = new SkillObjective(name, display, icon, currencyDrops, xpDrop, 0);
            objectives.add(objective);
        }
        return objectives;
    }

    @NotNull
    public static Set<SkillObjective> getEntityObjectives(@NotNull List<Tag<EntityType>> tags, double chance, double min, double max) {
        Set<EntityType> types = tags.stream().flatMap(t -> t.getValues().stream()).collect(Collectors.toSet());
        return getEntityObjectives(types, chance, min, max);
    }

    @NotNull
    public static Set<SkillObjective> getEntityObjectives(@NotNull Set<EntityType> types, double chance, double min, double max) {
        Set<SkillObjective> objectives = new HashSet<>();
        Collection<Currency> currencies = LootConomyAPI.PLUGIN.getCurrencyManager().getCurrencies();

        for (EntityType entityType : types) {
            String name = entityType.name();
            String display = LangManager.getEntityType(entityType);

            Material material = Material.getMaterial(entityType.name() + "_SPAWN_EGG");
            if (material == null) material = Material.BAT_SPAWN_EGG;

            ItemStack icon = new ItemStack(material);
            Map<String, DropInfo> currencyDrops = new HashMap<>();
            currencies.forEach(currency -> {
                if (!currency.getId().equalsIgnoreCase(EngineUtils.VAULT)) return;
                currencyDrops.put(currency.getId(), new DropInfo(chance, min, max));
            });
            DropInfo xpDrop = new DropInfo(chance, 1, 5);

            SkillObjective objective = new SkillObjective(name, display, icon, currencyDrops, xpDrop, 0);
            objectives.add(objective);
        }
        return objectives;
    }
}
