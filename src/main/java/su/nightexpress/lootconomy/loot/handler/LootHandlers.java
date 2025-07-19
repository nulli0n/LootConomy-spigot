package su.nightexpress.lootconomy.loot.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.api.loot.LootHandler;
import su.nightexpress.lootconomy.api.loot.LootProvider;
import su.nightexpress.lootconomy.hook.impl.ExcellentEnchantsHook;
import su.nightexpress.lootconomy.money.MoneyUtils;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.blocktracker.PlayerBlockTracker;

import java.util.function.Consumer;

public class LootHandlers {

    private static <O> void dropLoot(@NotNull LootConomyPlugin plugin,
                                     @NotNull Player player,
                                     @NotNull O object,
                                     @NotNull LootProvider<O> provider,
                                     @NotNull Location location) {
        Location center = LocationUtil.setCenter3D(location.clone());
        dropLoot(plugin, player, object, provider, itemStack -> {
            player.getWorld().dropItem(center, itemStack);
        });
    }

    private static <O> void dropLoot(@NotNull LootConomyPlugin plugin,
                                     @NotNull Player player,
                                     @NotNull O object,
                                     @NotNull LootProvider<O> provider,
                                     @NotNull Consumer<ItemStack> consumer) {
        provider.createLoot(plugin, player, object).forEach(consumer);
    }

    public static final LootHandler<BlockBreakEvent, Material> BLOCK_BREAK = (plugin, event, provider) -> {
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

            dropLoot(plugin, player, blockType, provider, itemStack -> {
                if (ExcellentEnchantsHook.hasTelekinesis(tool)) {
                    plugin.getMoneyManager().pickupMoney(player, itemStack);
                    return;
                }
                player.getWorld().dropItem(center, itemStack);
            });
        }
        return true;
    };

    public static final LootHandler<PlayerHarvestBlockEvent, Material> BLOCK_HARVEST = (plugin, event, provider) -> {
        Block block = event.getHarvestedBlock();
        if (PlayerBlockTracker.isTracked(block)) {
            return false;
        }

        Player player = event.getPlayer();
        //dropLoot(plugin, player, block.getType(), provider, block.getLocation());
        dropLoot(plugin, player, block.getType(), provider, itemStack -> {
            event.getItemsHarvested().add(itemStack);
        });
        return true;
    };

    public static final LootHandler<EntityDeathEvent, EntityType> ENTITY_KILL = (plugin, event, provider) -> {
        LivingEntity entity = event.getEntity();

        // Do not drop money if not killed by a player.
        Player killer = entity.getKiller();
        if (killer == null) return false;

        // Do not drop money if mob is handled by other plugin or if it was spawned by specific source.
        if (MoneyUtils.isDevastated(entity)) return false;
        if (!MoneyUtils.isVanillaMob(entity)) return false;

        // Do not drop money for mobs inside non-living (boats, minecarts) vehicles.
        Entity vehicle = entity.getVehicle();
        if (vehicle != null && !(vehicle instanceof LivingEntity)) return false;

        // Do not drop money if mob died from cramming.
        var lastCause = entity.getLastDamageCause();
        if (lastCause != null && lastCause.getDamageSource().getDamageType() == DamageType.CRAMMING) return false;

        dropLoot(plugin, killer, entity.getType(), provider, itemStack -> {
            event.getDrops().add(itemStack);
        });
        return true;
    };

//    public static final LootHandler<EntityDeathEvent, EntityType> ENTITY_SHOOT = (plugin, event, provider) -> {
//        LivingEntity entity = event.getEntity();
//        if (MoneyUtils.isDevastated(entity)) return false;
//
//        Player killer = entity.getKiller();
//        if (killer == null) return false;
//
//        if (!(entity.getLastDamageCause() instanceof EntityDamageByEntityEvent ede)) return false;
//        if (!(ede.getDamager() instanceof Projectile)) return false;
//
//        // Do not count MythicMobs here.
//        if (Plugins.isLoaded(HookId.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(entity)) return false;
//
//        dropLoot(plugin, killer, entity.getType(), provider, itemStack -> {
//            event.getDrops().add(itemStack);
//        });
//        return true;
//    };

    public static final LootHandler<PlayerShearEntityEvent, EntityType> ENTITY_SHEAR = (plugin, event, provider) -> {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();

        dropLoot(plugin, player, entity.getType(), provider, entity.getLocation());
        return true;
    };

    public static final LootHandler<PlayerFishEvent, Material> ITEM_FISH = (plugin, event, provider) -> {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;

        Entity caught = event.getCaught();
        if (!(caught instanceof Item item)) return false;

        Player player = event.getPlayer();
        dropLoot(plugin, player, item.getItemStack().getType(), provider, itemStack -> {
            Location locHook = event.getHook().getLocation();
            Location locPlayer = player.getLocation();

            Vector vec3d = (new Vector(locPlayer.getX() - locHook.getX(), locPlayer.getY() - locHook.getY(), locPlayer.getZ() - locHook.getZ())).multiply(0.1D);
            Item drop = player.getWorld().dropItem(caught.getLocation(), itemStack);
            drop.setVelocity(drop.getVelocity().add(vec3d));
        });
        return true;
    };
}
