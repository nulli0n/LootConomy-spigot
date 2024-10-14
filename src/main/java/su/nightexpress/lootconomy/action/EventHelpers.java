package su.nightexpress.lootconomy.action;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.lootconomy.hook.impl.ExcellentEnchantsHook;
import su.nightexpress.lootconomy.hook.impl.MythicMobsHook;
import su.nightexpress.lootconomy.money.MoneyUtils;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.blocktracker.PlayerBlockTracker;

import java.util.function.Consumer;

public class EventHelpers {

    private static <O> void dropLoot(@NotNull Player player, @NotNull O object, @NotNull LootProcessor<O> processor, @NotNull Location location) {
        Location center = LocationUtil.setCenter3D(location.clone());
        dropLoot(player, object, processor, itemStack -> {
            player.getWorld().dropItem(center, itemStack);
        });
    }

    private static <O> void dropLoot(@NotNull Player player, @NotNull O object, @NotNull LootProcessor<O> processor, @NotNull Consumer<ItemStack> consumer) {
        processor.getLoot(player, object, 1).forEach(consumer);
    }

    public static final EventHelper<BlockBreakEvent, Material> BLOCK_BREAK = (plugin, event, processor) -> {
        Block block = event.getBlock();
        BlockData blockData = block.getBlockData();
        Material blockType = block.getType();
        boolean isTall = blockType == Material.BAMBOO || blockType == Material.SUGAR_CANE;

        // Do not give money for ungrowth plants.
        if (!isTall && blockData instanceof Ageable age) {
            if (age.getAge() < age.getMaximumAge()) return false;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int blockHeight = isTall ? (blockType == Material.BAMBOO ? 16 : 4) : 1;
        for (int currentHeight = 0; currentHeight < blockHeight; currentHeight++) {
            if (currentHeight > 0) {
                block = block.getRelative(BlockFace.UP);
                if (block.getType() != blockType) break;
            }

            if (PlayerBlockTracker.isTracked(block)) {
                continue;
            }

            BlockBreakEvent event2 = new BlockBreakEvent(block, player);
            Location center = LocationUtil.setCenter3D(event2.getBlock().getLocation());

            dropLoot(player, blockType, processor, itemStack -> {
                if (ExcellentEnchantsHook.hasTelekinesis(tool)) {
                    plugin.getMoneyManager().pickupMoney(player, itemStack);
                    return;
                }
                player.getWorld().dropItem(center, itemStack);
            });
        }
        return true;
    };

    public static final EventHelper<PlayerHarvestBlockEvent, Material> BLOCK_HARVEST = (plugin, event, processor) -> {
        Block block = event.getHarvestedBlock();
        if (PlayerBlockTracker.isTracked(block)) {
            return false;
        }

        Player player = event.getPlayer();
        dropLoot(player, block.getType(), processor, block.getLocation());
        return true;
    };

    public static final EventHelper<BlockFertilizeEvent, Material> BLOCK_FERTILIZE = (plugin, event, processor) -> {
        Player player = event.getPlayer();
        if (player == null) return false;

        Block block = event.getBlock();
        dropLoot(player, block.getType(), processor, block.getLocation());

        event.getBlocks().forEach(blockState -> {
            dropLoot(player, blockState.getType(), processor, blockState.getLocation());
        });
        return true;
    };

    public static final EventHelper<EntityDeathEvent, EntityType> ENTITY_KILL = (plugin, event, processor) -> {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return false;
        if (MoneyUtils.isDevastated(entity)) return false;
        if (Plugins.isLoaded(HookId.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(entity)) return false;
        if (entity.getVehicle() instanceof Minecart || entity.getVehicle() instanceof Boat) return false;
        if (entity.getLastDamageCause() != null && entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.CRAMMING) return false;

        Player killer = entity.getKiller();
        if (killer == null) return false;

        dropLoot(killer, entity.getType(), processor, itemStack -> {
            event.getDrops().add(itemStack);
        });
        return true;
    };

    public static final EventHelper<EntityDeathEvent, EntityType> ENTITY_SHOOT = (plugin, event, processor) -> {
        LivingEntity entity = event.getEntity();
        if (MoneyUtils.isDevastated(entity)) return false;

        Player killer = entity.getKiller();
        if (killer == null) return false;

        if (!(entity.getLastDamageCause() instanceof EntityDamageByEntityEvent ede)) return false;
        if (!(ede.getDamager() instanceof Projectile)) return false;

        // Do not count MythicMobs here.
        if (Plugins.isLoaded(HookId.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(entity)) return false;

        dropLoot(killer, entity.getType(), processor, itemStack -> {
            event.getDrops().add(itemStack);
        });
        return true;
    };

    public static final EventHelper<PlayerShearEntityEvent, EntityType> ENTITY_SHEAR = (plugin, event, processor) -> {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        dropLoot(player, entity.getType(), processor, entity.getLocation());
        return true;
    };

    public static final EventHelper<PlayerFishEvent, Material> ITEM_FISH = (plugin, event, processor) -> {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;

        Entity caught = event.getCaught();
        if (!(caught instanceof Item item)) return false;

        Player player = event.getPlayer();
        dropLoot(player, item.getItemStack().getType(), processor, itemStack -> {
            Location locHook = event.getHook().getLocation();
            Location locPlayer = player.getLocation();

            Vector vec3d = (new Vector(locPlayer.getX() - locHook.getX(), locPlayer.getY() - locHook.getY(), locPlayer.getZ() - locHook.getZ())).multiply(0.1D);
            Item drop = player.getWorld().dropItem(caught.getLocation(), itemStack);
            drop.setVelocity(drop.getVelocity().add(vec3d));
        });
        return true;
    };
}
