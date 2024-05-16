package su.nightexpress.lootconomy.money.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.action.ActionType;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.data.impl.LootLimitData;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.money.object.MoneyObjective;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.lootconomy.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ObjectivesMenu extends ConfigMenu<LootConomyPlugin> implements AutoFilled<MoneyObjective>, Linked<ActionType<?, ?>> {

    public static final String FILE_NAME = "objectives.yml";

    private static final String GENERIC_OBJECTS = "%objects%";

    private final ItemHandler returnHandler;
    private final ViewLink<ActionType<?, ?>> link;

    private String       objectiveName;
    private List<String> objectiveLore;
    private List<String> objectsLore;
    private String       loreCurrencyLimit;
    private String       loreCurrencyAvail;
    private String       loreCurrencyPenalty;
    private int[]        objectSlots;

    public ObjectivesMenu(@NotNull LootConomyPlugin plugin) {
        super(plugin, FileConfig.loadOrExtract(plugin, Config.DIR_UI, FILE_NAME));
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            this.runNextTick(() -> plugin.getMoneyManager().openObjectivesMenu(viewer.getPlayer()));
        }));

        this.load();
    }

    @NotNull
    @Override
    public ViewLink<ActionType<?, ?>> getLink() {
        return link;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<MoneyObjective> autoFill) {
        Player player = viewer.getPlayer();
        ActionType<?, ?> type = this.getLink(player);
        LootUser user = plugin.getUserManager().getUserData(player);
        LootLimitData limitData = user.getLimitData();

        Collection<Booster> boosters = this.plugin.getBoosterManager().getBoosters(player);

        autoFill.setSlots(this.objectSlots);
        autoFill.setItems(this.plugin.getMoneyManager().getObjectives(type).stream().filter(MoneyObjective::canDrop).sorted(Comparator.comparing(MoneyObjective::getId)).toList());
        autoFill.setItemCreator(objective -> {
            List<String> objects = new ArrayList<>();
            for (String line : this.objectsLore) {
                if (line.contains(GENERIC_NAME)) {
                    objective.getObjects().stream().map(type::getObjectLocalizedName).sorted(String::compareTo).forEach(object -> {
                        objects.add(line.replace(GENERIC_NAME, object));
                    });
                }
                else objects.add(line);
            }

            List<String> rewards = new ArrayList<>();
            objective.getCurrencyDrops().forEach((id, drop) -> {
                if (drop.isEmpty()) return;

                Currency currency = this.plugin.getCurrencyManager().getCurrency(id);
                if (currency == null) return;

                double multiplier = Booster.getMultiplier(currency, boosters);

                String format;
                if (currency.hasDailyLimit() && limitData.isLimitExceed(currency)) {
                    format = this.loreCurrencyLimit;
                }
                else {
                    format = drop.isPenalty() ? this.loreCurrencyPenalty : this.loreCurrencyAvail;
                }

                if (drop.isPenalty()) {
                    multiplier = 1D;
                }

                double min = Math.abs(drop.getMinAmount()) * multiplier;
                double max = Math.abs(drop.getMaxAmount()) * multiplier;

                PlaceholderMap placeholderMap = new PlaceholderMap(currency.getPlaceholders());
                placeholderMap
                    .add(GENERIC_MIN, () -> currency.format(min))
                    .add(GENERIC_MAX, () -> currency.format(max))
                    .add(GENERIC_CHANCE, () -> NumberUtil.format(drop.getChance()));

                rewards.add(placeholderMap.replacer().apply(format));
            });

            ItemStack icon = objective.getIcon();
            ItemReplacer.create(icon).hideFlags().trimmed()
                .setDisplayName(this.objectiveName)
                .setLore(this.objectiveLore)
                .replaceLoreExact(GENERIC_CURRENCY, rewards)
                .replaceLoreExact(GENERIC_OBJECTS, objects)
                .replace(GENERIC_NAME, objective.getDisplayName())
                .writeMeta();

            return icon;
        });
        autoFill.setClickAction(objective -> (viewer1, event) -> {

        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Currency Objectives"), MenuSize.CHEST_54);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(45).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(53).setPriority(10).setHandler(ItemHandler.forNextPage(this)));

        ItemStack back = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(back, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(back).setSlots(49).setPriority(10).setHandler(this.returnHandler));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.objectiveName = ConfigValue.create("Objective.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(GENERIC_NAME))
        ).read(cfg);

        this.objectiveLore = ConfigValue.create("Objective.Lore.General", Lists.newList(
            GENERIC_OBJECTS,
            "",
            LIGHT_GREEN.enclose("✔") + LIGHT_GRAY.enclose(" - Possible reward."),
            LIGHT_RED.enclose("[❗]") + LIGHT_GRAY.enclose(" - Possible penalty."),
            "",
            LIGHT_YELLOW.enclose(BOLD.enclose("Loot:")),
            GENERIC_CURRENCY
        )).read(cfg);

        this.objectsLore = ConfigValue.create("Objective.Objects", Lists.newList(
            LIGHT_GRAY.enclose(GENERIC_NAME)
        )).read(cfg);

        this.loreCurrencyAvail = ConfigValue.create("Objective.Lore.Currency.Available",
            LIGHT_GREEN.enclose("✔ " + LIGHT_GRAY.enclose(CURRENCY_NAME + ": ") + GENERIC_MIN + LIGHT_GRAY.enclose(" ⬌ ") + GENERIC_MAX + " " + GRAY.enclose("(" + WHITE.enclose(GENERIC_CHANCE + "%)")))
        ).read(cfg);

        this.loreCurrencyPenalty = ConfigValue.create("Objective.Lore.Currency.Penalty",
            LIGHT_RED.enclose("[❗] " + LIGHT_GRAY.enclose(CURRENCY_NAME + ": ") + GENERIC_MIN + LIGHT_GRAY.enclose(" ⬌ ") + GENERIC_MAX + " " + GRAY.enclose("(" + WHITE.enclose(GENERIC_CHANCE + "%)")))
        ).read(cfg);

        this.loreCurrencyLimit = ConfigValue.create("Objective.Lore.Currency.LimitReached",
            LIGHT_ORANGE.enclose("✘ " + LIGHT_GRAY.enclose(CURRENCY_NAME + ": ") + GENERIC_MIN + LIGHT_GRAY.enclose(" ⬌ ") + GENERIC_MAX + " " + LIGHT_RED.enclose("(Daily Limit)"))
        ).read(cfg);

        this.objectSlots = ConfigValue.create("Objective.Slots", IntStream.range(0, 45).toArray()).read(cfg);
    }
}
