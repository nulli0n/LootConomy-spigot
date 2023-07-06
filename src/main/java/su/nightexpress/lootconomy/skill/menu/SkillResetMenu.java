package su.nightexpress.lootconomy.skill.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.data.impl.SkillData;

import java.util.Map;
import java.util.WeakHashMap;

public class SkillResetMenu extends ConfigMenu<LootConomy> {

    private static final Map<Player, SkillData> CACHE = new WeakHashMap<>();

    public SkillResetMenu(@NotNull LootConomy plugin) {
        super(plugin, JYML.loadOrExtract(plugin, "/menu/skill_reset.yml"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CONFIRMATION_DECLINE, (viewer, event) -> {
                plugin.getSkillManager().getSkillListMenu().openNextTick(viewer, 1);
            })
            .addClick(MenuItemType.CONFIRMATION_ACCEPT, (viewer, event) -> {
                Player player = viewer.getPlayer();
                SkillData skillData = CACHE.remove(player);
                if (skillData == null) return;

                skillData.reset();
                plugin.getMessage(Lang.SKILL_RESET_NOTIFY).replace(skillData.replacePlaceholders()).send(player);
                player.closeInventory();
            });

        this.load();

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                SkillData skillData = CACHE.get(viewer.getPlayer());
                if (skillData == null) return;

                ItemUtil.replace(item, skillData.replacePlaceholders());
            });
        });
    }

    public void open(@NotNull Player player, @NotNull SkillData data) {
        CACHE.put(player, data);
        this.open(player, 1);
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);
        CACHE.remove(viewer.getPlayer());
    }
}
