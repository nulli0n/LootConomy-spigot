package su.nightexpress.lootconomy.money.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.currency.CurrencySettings;
import su.nightexpress.lootconomy.data.impl.LootLimitData;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.loot.handler.LootAction;
import su.nightexpress.lootconomy.loot.handler.LootActions;
import su.nightexpress.lootconomy.loot.objective.ObjectiveCategory;
import su.nightexpress.lootconomy.money.object.MoneyObjective;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.lootconomy.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class ObjectivesMenu extends LinkedMenu<LootConomyPlugin, ObjectiveCategory> implements Filled<MoneyObjective>, ConfigBased {

    public static final String FILE_NAME = "objective_items.yml";

    private static final String GENERIC_OBJECTS = "%objects%";
    private static final String GENERIC_REWARDS = "%rewards%";

    private String       objectiveName;
    private List<String> objectiveLore;
    private int[]        objectiveSlots;

    private String       objectEntry;
    private String       objectCurrencyLimit;
    private String       objectCurrencyGood;
    private String       objectCurrencyBad;

    public ObjectivesMenu(@NotNull LootConomyPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.enclose("Currency Objectives"));

        this.load(FileConfig.loadOrExtract(plugin, Config.DIR_UI, FILE_NAME));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    public MenuFiller<MoneyObjective> createFiller(@NotNull MenuViewer viewer) {
        var autoFill = MenuFiller.builder(this);

        Player player = viewer.getPlayer();
        ObjectiveCategory category = this.getLink(player);
        LootUser user = plugin.getUserManager().getOrFetch(player);
        LootLimitData limitData = user.getLimitData();

        Collection<Booster> boosters = this.plugin.getBoosterManager().getBoosters(player);

        autoFill.setSlots(this.objectiveSlots);
        autoFill.setItems(this.plugin.getMoneyManager().getObjectives(category).stream().filter(MoneyObjective::canDrop).sorted(Comparator.comparing(MoneyObjective::getId)).toList());
        autoFill.setItemCreator(objective -> {
            LootAction<?, ?> action = LootActions.getByName(objective.getActionName());
            if (action == null) return new NightItem(Material.AIR);

            List<String> objects = new ArrayList<>();
            objective.getObjects().forEach(objectId -> {
                String localized = action.getObjectLocalizedName(objectId);

                objects.add(Replacer.create().replace(GENERIC_NAME, localized).apply(this.objectEntry));
            });

            List<String> rewards = new ArrayList<>();
            objective.getCurrencyDrops().forEach((id, drop) -> {
                if (drop.isEmpty()) return;

                Currency currency = EconomyBridge.getCurrency(id);
                if (currency == null) return;

                CurrencySettings settings = plugin.getCurrencyManager().getSettings(currency);
                if (settings == null) return;

                double multiplier = drop.isPenalty() ? 1D : Booster.getMultiplier(currency, boosters);

                String format;
                if (settings.hasDailyLimit() && limitData.isLimitExceed(currency, settings)) {
                    format = this.objectCurrencyLimit;
                }
                else {
                    format = drop.isPenalty() ? this.objectCurrencyBad : this.objectCurrencyGood;
                }

                double min = Math.abs(drop.getMinAmount()) * multiplier;
                double max = Math.abs(drop.getMaxAmount()) * multiplier;

                rewards.add(Replacer.create()
                    .replace(GENERIC_MIN, () -> currency.format(min))
                    .replace(GENERIC_MAX, () -> currency.format(max))
                    .replace(GENERIC_CHANCE, () -> NumberUtil.format(drop.getChance()))
                    .replace(currency.replacePlaceholders())
                    .apply(format)
                );
            });

            return objective.getIcon()
                .setHideComponents(true)
                .setDisplayName(this.objectiveName)
                .setLore(this.objectiveLore)
                .replacement(replacer -> replacer
                    .replace(GENERIC_REWARDS, rewards)
                    .replace(GENERIC_OBJECTS, objects)
                    .replace(GENERIC_NAME, objective.getDisplayName())
                );
        });

        return autoFill.build();
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.objectiveName = ConfigValue.create("Objective.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(GENERIC_NAME))
        ).read(config);

        this.objectiveLore = ConfigValue.create("Objective.Lore", Lists.newList(
            GENERIC_OBJECTS,
            EMPTY_IF_ABOVE,
            GENERIC_REWARDS
        )).read(config);

        this.objectEntry = ConfigValue.create("Objective.Objects.Entry",
            LIGHT_GRAY.enclose(GENERIC_NAME)
        ).read(config);

        this.objectCurrencyGood = ConfigValue.create("Objective.Objects.Currency.Available",
            GREEN.enclose("↑ " + GENERIC_MIN + GRAY.enclose(" ⬌ ") + GENERIC_MAX + " " + GRAY.enclose("(" + WHITE.enclose(GENERIC_CHANCE + "%") + ")"))
        ).read(config);

        this.objectCurrencyBad = ConfigValue.create("Objective.Objects.Currency.Penalty",
            RED.enclose("↓ " + GENERIC_MIN + GRAY.enclose(" ⬌ ") + GENERIC_MAX + " " + GRAY.enclose("(" + WHITE.enclose(GENERIC_CHANCE + "%") + ")"))
        ).read(config);

        this.objectCurrencyLimit = ConfigValue.create("Objective.Objects.Currency.LimitReached",
            YELLOW.enclose("⏳ " + GENERIC_MIN + GRAY.enclose(" ⬌ ") + GENERIC_MAX + " " + YELLOW.enclose("(Max. Today)"))
        ).read(config);

        this.objectiveSlots = ConfigValue.create("Objective.Slots", IntStream.range(0, 45).toArray()).read(config);

        loader.addDefaultItem(MenuItem.buildNextPage(this, 53));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 45));
        loader.addDefaultItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            this.runNextTick(() -> plugin.getMoneyManager().openObjectivesMenu(viewer.getPlayer()));
        }));
    }
}
