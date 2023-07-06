package su.nightexpress.lootconomy.skill.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.data.impl.SkillLimitData;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.skill.impl.SkillObjective;
import su.nightexpress.lootconomy.skill.util.DropInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SkillObjectivesMenu extends ConfigMenu<LootConomy> implements AutoPaged<SkillObjective> {

    private static final String PLACEHOLDER_LOCKED         = "%locked%";
    private static final String PLACEHOLDER_UNLOCKED       = "%unlocked%";
    private static final String PLACEHOLDER_LIMIT_XP       = "%limit_xp%";
    private static final String PLACEHOLDER_LIMIT_CURRENCY = "%limit_currency%";

    private final Skill        skill;
    private final String       objName;
    private final List<String> objLore;
    private final List<String> loreLimitXP;
    private final List<String> loreLimitCurrency;
    private final List<String> loreLocked;
    private final List<String> loreUnlocked;
    private final int[]        objSlots;

    public SkillObjectivesMenu(@NotNull LootConomy plugin, @NotNull Skill skill) {
        super(plugin, JYML.loadOrExtract(plugin, "/menu/skill_objectives.yml"));

        this.skill = skill;
        this.objName = Colorizer.apply(cfg.getString("Objective.Name", Placeholders.OBJECTIVE_NAME));
        this.objLore = Colorizer.apply(cfg.getStringList("Objective.Lore"));
        this.loreLimitXP = Colorizer.apply(cfg.getStringList("Objective.Lore_Format.Limit_XP"));
        this.loreLimitCurrency = Colorizer.apply(cfg.getStringList("Objective.Lore_Format.Limit_Currency"));
        this.loreLocked = Colorizer.apply(cfg.getStringList("Objective.Lore_Format.Locked"));
        this.loreUnlocked = Colorizer.apply(cfg.getStringList("Objective.Lore_Format.Unlocked"));
        this.objSlots = cfg.getIntArray("Objective.Slots");

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.RETURN, (viewer, event) -> {
                plugin.getSkillManager().getSkillListMenu().openNextTick(viewer, 1);
            })
            .addClick(MenuItemType.CLOSE, (viewer, event) -> viewer.getPlayer().closeInventory())
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.load();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);

        options.setTitle(this.skill.replacePlaceholders().apply(options.getTitle()));

        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    public int[] getObjectSlots() {
        return this.objSlots;
    }

    @Override
    @NotNull
    public List<SkillObjective> getObjects(@NotNull Player player) {
        return this.skill.getObjectiveMap().values().stream().sorted(Comparator.comparing(SkillObjective::getName)).toList();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull SkillObjective objective) {
        LootUser user = plugin.getUserManager().getUserData(player);
        SkillData skillData = user.getData(this.skill);
        SkillLimitData limitData = skillData.getLimitData();
        boolean isUnlocked = objective.isUnlocked(player, skillData);

        List<String> lore = new ArrayList<>(this.objLore);

        lore = StringUtil.replace(lore, PLACEHOLDER_LOCKED, false, !isUnlocked ? this.loreLocked : Collections.emptyList());
        lore = StringUtil.replace(lore, PLACEHOLDER_UNLOCKED, false, isUnlocked ? this.loreUnlocked : Collections.emptyList());
        lore = StringUtil.replace(lore, PLACEHOLDER_LIMIT_XP, false, skillData.isXPLimitReached() ? this.loreLimitXP : Collections.emptyList());
        List<String> curLimit = new ArrayList<>();
        for (Currency currency : plugin.getCurrencyManager().getCurrencies()) {
            if (skillData.isCurrencyLimitReached(currency)) {
                List<String> limit = new ArrayList<>(this.loreLimitCurrency);
                limit.replaceAll(currency.replacePlaceholders());
                curLimit.addAll(limit);
            }
        }
        lore = StringUtil.replace(lore, PLACEHOLDER_LIMIT_CURRENCY, false, curLimit);

        List<String> loreFinal = new ArrayList<>();
        for (String line : lore) {
            if (line.contains(Placeholders.OBJECTIVE_CURRENCY_MIN)) {
                for (Currency currency : plugin.getCurrencyManager().getCurrencies()) {
                    DropInfo dropInfo = objective.getCurrencyDrop(currency);
                    if (dropInfo.isEmpty()) continue;

                    loreFinal.add(currency.replacePlaceholders().apply(line)
                        .replace(Placeholders.OBJECTIVE_CURRENCY_MIN, NumberUtil.format(dropInfo.getMin()))
                        .replace(Placeholders.OBJECTIVE_CURRENCY_MAX, NumberUtil.format(dropInfo.getMax()))
                        .replace(Placeholders.OBJECTIVE_CURRENCY_CHANCE, NumberUtil.format(dropInfo.getChance()))
                    );
                }
            }
            else if (line.contains(Placeholders.OBJECTIVE_XP_MIN)) {
                loreFinal.add(line
                    .replace(Placeholders.OBJECTIVE_XP_MIN, NumberUtil.format(objective.getXPDrop().getMin()))
                    .replace(Placeholders.OBJECTIVE_XP_MAX, NumberUtil.format(objective.getXPDrop().getMax()))
                    .replace(Placeholders.OBJECTIVE_XP_CHANCE, NumberUtil.format(objective.getXPDrop().getChance()))
                );
            }
            else loreFinal.add(line
                    .replace(Placeholders.OBJECTIVE_UNLOCK_LEVEL, NumberUtil.format(objective.getUnlockLevel()))
                );
        }

        ItemStack item = objective.getIcon();
        ItemUtil.mapMeta(item, meta -> {
            meta.setDisplayName(this.objName.replace(Placeholders.OBJECTIVE_NAME, objective.getDisplayName()));
            meta.setLore(loreFinal);
            meta.addItemFlags(ItemFlag.values());
        });

        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull SkillObjective lootObjective) {
        return (viewer, event) -> {

        };
    }

    /*@NotNull
    private String getName(@NotNull SkillObjective objective) {
        String typeRaw = objective.getName().toUpperCase();

        Material material = Material.getMaterial(typeRaw);
        if (material != null) {
            return LangManager.getMaterial(material);
        }

        if (Hooks.hasPlugin(HookId.MYTHIC_MOBS) && MythicMobsHook.getMobConfig(typeRaw) != null) {
            return MythicMobsHook.getMobDisplayName(typeRaw);
        }

        EntityType entityType = StringUtil.getEnum(typeRaw, EntityType.class).orElse(null);
        if (entityType != null) {
            return LangManager.getEntityType(entityType);
        }

        return objective.getName();
    }

    private boolean setMaterial(@NotNull ItemStack item, @NotNull SkillObjective object) {
        Material material = Material.getMaterial(object.getName().toUpperCase());
        if (material != null) {
            if (material == Material.CARROTS) material = Material.CARROT;
            else if (material == Material.POTATOES) material = Material.POTATO;
            else if (material == Material.COCOA) material = Material.COCOA_BEANS;
            else if (material == Material.BEETROOTS) material = Material.BEETROOT;
            else if (material == Material.SWEET_BERRY_BUSH) material = Material.SWEET_BERRIES;
            else if (material == Material.KELP_PLANT) material = Material.KELP;

            item.setType(material);
            return true;
        }
        return false;
    }

    private void setEntity(@NotNull ItemStack item, @NotNull SkillObjective object) {
        String typeRaw = object.getName().toUpperCase();

        if (this.skill.getType() == SkillType.KILL_MYTHIC_MOB && Hooks.hasPlugin(HookId.MYTHIC_MOBS)) {
            MythicMob mythicMob = MythicMobsHook.getMobConfig(typeRaw);
            if (mythicMob != null) typeRaw = mythicMob.getEntityType();
        }

        EntityType entityType = StringUtil.getEnum(typeRaw, EntityType.class).orElse(null);
        if (entityType == null) return;

        Material material = Material.getMaterial(entityType.name() + "_SPAWN_EGG");
        if (material != null) {
            item.setType(material);
            return;
        }

        switch (entityType) {
            case GIANT -> item.setType(Material.ZOMBIE_HEAD);
            case SNOWMAN -> item.setType(Material.PUMPKIN);
            case ILLUSIONER -> item.setType(Material.VILLAGER_SPAWN_EGG);
            case ENDER_DRAGON -> item.setType(Material.DRAGON_HEAD);
            case IRON_GOLEM -> item.setType(Material.IRON_BLOCK);
            case WITHER -> item.setType(Material.WITHER_SKELETON_SKULL);
            case MUSHROOM_COW -> item.setType(Material.RED_MUSHROOM_BLOCK);
            default -> {}
        }
    }

    private void fineMaterial(@NotNull ItemStack item, @NotNull SkillObjective object) {
        switch (this.skill.getType()) {
            case FISHING -> {
                if (this.setMaterial(item, object)) return;
                this.setEntity(item, object);
            }
            case BLOCK_BREAK -> this.setMaterial(item, object);
            case KILL_MYTHIC_MOB, KILL_MOB -> this.setEntity(item, object);
            default -> {}
        }
    }*/
}
