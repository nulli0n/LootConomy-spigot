package su.nightexpress.lootconomy.money.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.money.MoneyUtils;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.util.blocktracker.PlayerBlockTracker;

public class AbuseListener extends AbstractListener<LootConomyPlugin> {

    public AbuseListener(@NotNull LootConomyPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQoLTamedMobDevastate(EntityTameEvent event) {
        if (!Config.ABUSE_NO_DROP_FROM_TAMED_MOBS.get()) return;

        MoneyUtils.devastateEntity(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbuseEntitySpawn(CreatureSpawnEvent event) {
        if (Config.ABUSE_IGNORE_SPAWN_REASONS.get().contains(event.getSpawnReason())) {
            MoneyUtils.devastateEntity(event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbuseEntityTransform(EntityTransformEvent event) {
        if (MoneyUtils.isDevastated(event.getEntity())) {
            event.getTransformedEntities().forEach(MoneyUtils::devastateEntity);
            MoneyUtils.devastateEntity(event.getTransformedEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbuseBlockFertilize(BlockFertilizeEvent event) {
        if (Config.ABUSE_IGNORE_FERTILIZED.get().contains(event.getBlock().getType())) {
            PlayerBlockTracker.trackForce(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbuseBlockGeneration(BlockFormEvent event) {
        if (Config.ABUSE_IGNORE_BLOCK_GENERATION.get().contains(event.getNewState().getType())) {
            plugin.runTask(task -> {
                PlayerBlockTracker.trackForce(event.getBlock());
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAbuseHopperPickup(InventoryPickupItemEvent event) {
        if (event.getInventory().getType() != InventoryType.HOPPER) return;

        ItemStack item = event.getItem().getItemStack();
        if (MoneyUtils.isMoney(item)) {
            event.setCancelled(true);
        }
    }
}
