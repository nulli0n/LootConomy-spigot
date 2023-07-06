package su.nightexpress.lootconomy.skill.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.skill.impl.Rank;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SkillListMenu extends ConfigMenu<LootConomy> implements AutoPaged<SkillData> {

    private final String       skillNameAvailable;
    private final List<String> skillLoreAvailable;
    private final String       skillNameLockedPerm;
    private final List<String> skillLoreLockedPerm;
    private final int[]        skillSlots;

    public SkillListMenu(@NotNull LootConomy plugin) {
        super(plugin, JYML.loadOrExtract(plugin, "/menu/skill_list.yml"));

        this.skillNameAvailable = Colorizer.apply(cfg.getString("Skill.Available.Name", ""));
        this.skillLoreAvailable = Colorizer.apply(cfg.getStringList("Skill.Available.Lore"));
        this.skillNameLockedPerm = Colorizer.apply(cfg.getString("Skill.Locked_Permission.Name", ""));
        this.skillLoreLockedPerm = Colorizer.apply(cfg.getStringList("Skill.Locked_Permission.Lore"));
        this.skillSlots = cfg.getIntArray("Skill.Slots");

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, (viewer, event) -> viewer.getPlayer().closeInventory())
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.load();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    public int[] getObjectSlots() {
        return this.skillSlots;
    }

    @Override
    @NotNull
    public List<SkillData> getObjects(@NotNull Player player) {
        return plugin.getUserManager().getUserData(player).getDatas()
            .stream().sorted(Comparator.comparing(data -> data.getSkill().getId())).toList();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull SkillData skillData) {
        Skill skill = skillData.getSkill();
        Rank rank = skillData.getRank();
        int level = skillData.getLevel();
        Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player, skill);

        boolean hasAccess = skill.hasPermission(player);
        List<String> lore;
        String name;
        if (hasAccess) {
            name = this.skillNameAvailable;
            lore = new ArrayList<>(this.skillLoreAvailable);
        }
        else {
            name = this.skillNameLockedPerm;
            lore = new ArrayList<>(this.skillLoreLockedPerm);
        }

        List<String> loreFinal = new ArrayList<>();
        for (String line : lore) {
            if (line.contains(Placeholders.CURRENCY_MULTIPLIER)) {
                for (Currency currency : plugin.getCurrencyManager().getCurrencies()) {
                    loreFinal.add(currency.replacePlaceholders().apply(line)
                        .replace(Placeholders.CURRENCY_MULTIPLIER, NumberUtil.format(rank.getCurrencyMultiplier(currency, level)))
                    );
                }
            }
            else if (line.contains(Placeholders.CURRENCY_BOOST_PERCENT) || line.contains(Placeholders.CURRENCY_BOOST_MODIFIER)) {
                for (Currency currency : plugin.getCurrencyManager().getCurrencies()) {
                    loreFinal.add(currency.replacePlaceholders().apply(line)
                        .replace(Placeholders.CURRENCY_BOOST_MODIFIER, NumberUtil.format(Booster.getCurrencyBoost(currency, boosters)))
                        .replace(Placeholders.CURRENCY_BOOST_PERCENT, NumberUtil.format(Booster.getCurrencyPercent(currency, boosters)))
                    );
                }
            }
            else {
                loreFinal.add(line
                    .replace(Placeholders.XP_BOOST_MODIFIER, NumberUtil.format(Booster.getXPBoost(boosters)))
                    .replace(Placeholders.XP_BOOST_PERCENT, NumberUtil.format(Booster.getXPPercent(boosters)))
                    .replace(Placeholders.XP_MULTIPLIER, NumberUtil.format(rank.getXPMultiplier(level)))
                );
            }
        }

        ItemStack item = skill.getIcon();
        ItemUtil.mapMeta(item, meta -> {
            meta.setDisplayName(name);
            meta.setLore(loreFinal);
            meta.addItemFlags(ItemFlag.values());
            ItemUtil.replace(meta, skillData.replacePlaceholders());
            ItemUtil.replace(meta, skill.replacePlaceholders());
            ItemUtil.replace(meta, rank.replacePlaceholders());
        });

        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull SkillData skillData) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            Skill skill = skillData.getSkill();

            if (!skill.hasPermission(player)) {
                plugin.getMessage(EngineLang.ERROR_PERMISSION_DENY).send(player);
                return;
            }

            if (event.getClick() == ClickType.DROP && Config.LEVELING_ENABLED.get()) {
                if (!player.hasPermission(Perms.COMMAND_RESET)) {
                    plugin.getMessage(EngineLang.ERROR_PERMISSION_DENY).send(player);
                    return;
                }
                plugin.getSkillManager().getSkillResetMenu().open(player, skillData);
                return;
            }

            skill.getObjectivesMenu().open(player, 1);
        };
    }
}
