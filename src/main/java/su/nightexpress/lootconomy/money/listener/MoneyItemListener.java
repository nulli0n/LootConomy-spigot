package su.nightexpress.lootconomy.money.listener;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.money.MoneyManager;
import su.nightexpress.lootconomy.money.MoneyUtils;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Players;

public class MoneyItemListener extends AbstractListener<LootConomyPlugin> {

    private final MoneyManager moneyManager;

    public MoneyItemListener(@NotNull LootConomyPlugin plugin, @NotNull MoneyManager moneyManager) {
        super(plugin);
        this.moneyManager = moneyManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoneyItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();
        Currency currency = this.moneyManager.getCurrency(itemStack);
        if (currency == null) return;

        this.moneyManager.getTrackedLoot().add(item);
        //item.setCustomName(ItemUtil.getItemName(itemStack));
        EntityUtil.setCustomName(item, ItemUtil.getNameSerialized(itemStack));
        item.setCustomNameVisible(true);
    }

    // Support for plugins like RPGLoot
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoneyItemInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || !MoneyUtils.isMoney(item)) return;

        event.setCancelled(true);

        if (this.moneyManager.pickupMoney((Player) event.getWhoClicked(), item)) {
            event.setCurrentItem(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoneyItemPickup(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (!MoneyUtils.isMoney(item)) return;

        event.setCancelled(true);

        if (!(event.getEntity() instanceof Player player)) return;
        if (!Players.isReal(player) || !MoneyUtils.isOwner(item, player)) return;

        if (this.moneyManager.pickupMoney(player, item)) {
            event.getItem().remove();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeathPenalty(PlayerDeathEvent event) {
        Player player = event.getEntity();

        this.moneyManager.createDeathPenalty(player).forEach(itemStack -> {
            event.getDrops().add(itemStack);
        });
    }

    /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrewingClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof BrewerInventory inventory)) return;

        BrewingStand stand = inventory.getHolder();
        if (stand == null) return;

        PDCUtil.set(stand, Keys.brewingHolder, event.getWhoClicked().getUniqueId().toString());
        stand.update();
    }*/
}
