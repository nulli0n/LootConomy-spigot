package su.nightexpress.lootconomy.money.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.action.ActionType;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
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
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static su.nightexpress.lootconomy.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ObjectiveTypesMenu extends ConfigMenu<LootConomyPlugin> implements AutoFilled<ActionType<?, ?>> {

    public static final String FILE_NAME = "objective_types.yml";

    private String       objectName;
    private List<String> objectLore;
    private int[]        objectSlots;

    public ObjectiveTypesMenu(@NotNull LootConomyPlugin plugin) {
        super(plugin, FileConfig.loadOrExtract(plugin, Config.DIR_UI, FILE_NAME));

        this.load();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<ActionType<?, ?>> autoFill) {
        autoFill.setSlots(this.objectSlots);
        autoFill.setItems(this.plugin.getActionRegistry().getActionTypes().stream().sorted(Comparator.comparing(ActionType::getName)).toList());
        autoFill.setItemCreator(type -> {
            ItemStack icon = type.getIcon();
            ItemReplacer.create(icon).hideFlags().trimmed()
                .setDisplayName(this.objectName)
                .setLore(this.objectLore)
                .replace(GENERIC_NAME, type.getDisplayName())
                .replace(GENERIC_AMOUNT, () -> NumberUtil.format(plugin.getMoneyManager().getObjectives(type).size()))
                .writeMeta();

            return icon;
        });
        autoFill.setClickAction(type -> (viewer1, event) -> {
            this.runNextTick(() -> plugin.getMoneyManager().openObjectivesMenu(viewer1.getPlayer(), type));
        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Objective Types"), MenuSize.CHEST_54);
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

        ItemStack back = ItemUtil.getSkinHead(SKIN_WRONG_MARK);
        ItemUtil.editMeta(back, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_CLOSE.getDefaultName());
        });
        list.add(new MenuItem(back).setSlots(49).setPriority(10).setHandler(ItemHandler.forClose(this)));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.objectName = ConfigValue.create("Object.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(GENERIC_NAME))
        ).read(cfg);

        this.objectLore = ConfigValue.create("Object.Lore", Lists.newList(
            LIGHT_GRAY.enclose("There are " + LIGHT_YELLOW.enclose(GENERIC_AMOUNT) + " objectives"),
            LIGHT_GRAY.enclose("in this category.")
        )).read(cfg);

        this.objectSlots = ConfigValue.create("Object.Slots", new int[] {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34}).read(cfg);
    }
}
