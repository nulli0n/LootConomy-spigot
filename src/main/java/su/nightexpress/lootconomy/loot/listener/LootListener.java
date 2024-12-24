package su.nightexpress.lootconomy.loot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.loot.handler.LootActions;
import su.nightexpress.nightcore.manager.AbstractListener;

public class LootListener extends AbstractListener<LootConomyPlugin> {

    public LootListener(LootConomyPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // TODO Use BlockDropItem event
        LootActions.MINING.handle(plugin, event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockHarvest(PlayerHarvestBlockEvent event) {
        LootActions.GATHERING.handle(plugin, event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LootActions.MOB_KILL.handle(plugin, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShear(PlayerShearEntityEvent event) {
        LootActions.SHEARING.handle(plugin, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFishing(PlayerFishEvent event) {
        LootActions.FISHING.handle(plugin, event);
    }
}
