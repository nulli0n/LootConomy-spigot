package su.nightexpress.lootconomy.money.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.loot.objective.ObjectiveCategory;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.Comparator;
import java.util.List;

import static su.nightexpress.lootconomy.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

@SuppressWarnings("UnstableApiUsage")
public class CategoriesMenu extends NormalMenu<LootConomyPlugin> implements Filled<ObjectiveCategory>, ConfigBased {

    public static final String FILE_NAME = "objective_categories.yml";

    private String       objectName;
    private List<String> objectLore;
    private int[]        objectSlots;

    public CategoriesMenu(@NotNull LootConomyPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X3, BLACK.wrap("Objective Categories"));

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
    public MenuFiller<ObjectiveCategory> createFiller(@NotNull MenuViewer viewer) {
        var autoFill = MenuFiller.builder(this);

        autoFill.setSlots(this.objectSlots);
        autoFill.setItems(this.plugin.getMoneyManager().getCategories().stream().sorted(Comparator.comparing(ObjectiveCategory::getName)).toList());
        autoFill.setItemCreator(category -> {
            return category.getIcon()
                .setHideComponents(true)
                .setDisplayName(this.objectName)
                .setLore(this.objectLore)
                .replacement(replacer -> replacer
                    .replace(category.replacePlaceholders())
                    .replace(GENERIC_AMOUNT, () -> NumberUtil.format(plugin.getMoneyManager().getObjectives(category).size()))
                );
        });
        autoFill.setItemClick(type -> (viewer1, event) -> {
            this.runNextTick(() -> plugin.getMoneyManager().openObjectivesMenu(viewer1.getPlayer(), type));
        });

        return autoFill.build();
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.objectName = ConfigValue.create("Object.Name",
            LIGHT_YELLOW.wrap(BOLD.wrap(CATEGORY_NAME))
        ).read(config);

        this.objectLore = ConfigValue.create("Object.Lore", Lists.newList(
            CATEGORY_DESCRIPTION,
            EMPTY_IF_ABOVE,
            LIGHT_GRAY.wrap("There are " + LIGHT_YELLOW.wrap(GENERIC_AMOUNT) + " objectives"),
            LIGHT_GRAY.wrap("in this category.")
        )).read(config);

        this.objectSlots = ConfigValue.create("Object.Slots", new int[] {10,11,12,13,14,15,16}).read(config);

        loader.addDefaultItem(MenuItem.buildNextPage(this, 26));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 18));
    }
}
