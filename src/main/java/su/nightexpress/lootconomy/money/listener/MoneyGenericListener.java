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
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.money.MoneyManager;

public class MoneyGenericListener extends AbstractListener<LootConomy> {

    private final MoneyManager moneyManager;

    public MoneyGenericListener(@NotNull MoneyManager moneyManager) {
        super(moneyManager.plugin());
        this.moneyManager = moneyManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoneyItemSpawn(ItemSpawnEvent e) {
        Item item = e.getEntity();
        ItemStack itemStack = item.getItemStack();
        Currency currency = MoneyManager.getMoneyCurrency(itemStack);
        if (currency == null) return;

        this.moneyManager.getTrackedLoot().add(item);
        item.setCustomName(ItemUtil.getItemName(itemStack));
        item.setCustomNameVisible(true);
    }

    // Support for plugins like RPGLoot
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMoneyItemInventoryClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null || !MoneyManager.isMoneyItem(item)) return;

        e.setCancelled(true);

        if (this.moneyManager.pickupMoney((Player) e.getWhoClicked(), item)) {
            e.setCurrentItem(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoneyItemPickup(EntityPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (!MoneyManager.isMoneyItem(item)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof Player player)) return;
        if (EntityUtil.isNPC(player) || !MoneyManager.isMoneyOwner(item, player)) return;

        if (this.moneyManager.pickupMoney(player, item)) {
            e.getItem().remove();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMoneyPenaltyPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (EntityUtil.isNPC(player)) return;
        if (player.hasPermission(Perms.BYPASS_DEATH_PENALTY)) return;
        if (!MoneyManager.isMoneyAvailable(player)) return;

        plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
            if (!currency.isDeathPenaltyEnabled()) return;
            if (Rnd.get(true) >= currency.getDeathPenaltyChance()) return;

            double amountPercent = currency.getDeathPenaltyAmount();
            if (amountPercent <= 0) return;

            double balance = currency.getHandler().getBalance(player);
            double amountLost = NumberUtil.round(balance * amountPercent / 100D);
            if (amountLost <= 0D) return;

            if (this.moneyManager.loseMoney(player, currency, amountLost)) {
                if (currency.isDeathPenaltyDropItem()) {
                    ItemStack item = MoneyManager.createMoney(currency, amountLost, null, null, null);
                    e.getDrops().add(item);
                }
            }
        });
    }
}
