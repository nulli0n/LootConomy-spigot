package su.nightexpress.lootconomy.money.task;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.money.MoneyManager;

public class InventoryCheckTask extends AbstractTask<LootConomy> {

    public InventoryCheckTask(@NotNull MoneyManager moneyManager) {
        super(moneyManager.plugin(), Config.GENERAL_FULL_INV_PICKUP_INTERVAL.get(), false);
    }

    @Override
    public void action() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getInventory().firstEmpty() != -1) continue;
            if (!MoneyManager.isMoneyAvailable(player)) continue;

            player.getNearbyEntities(2, 2, 2).stream().filter(e -> e instanceof Item).forEach(e -> {
                Item item = (Item) e;
                ItemStack stack = item.getItemStack();
                if (MoneyManager.isMoneyItem(stack)) {
                    EntityPickupItemEvent event = new EntityPickupItemEvent(player, item, 0);
                    plugin.getPluginManager().callEvent(event);
                }
            });
        }
    }
}
