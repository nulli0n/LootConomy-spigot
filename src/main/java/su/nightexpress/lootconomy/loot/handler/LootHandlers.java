package su.nightexpress.lootconomy.loot.handler;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class LootHandlers {

    /**
     * Core helper that retrieves loot from the provider and hands each ItemStack
     * to the supplied consumer. Logs any null / AIR items or exceptions.
     */
    private static <O> void dropLoot(@NotNull LootConomyPlugin plugin,
                                     @NotNull Player player,
                                     @NotNull O object,
                                     @NotNull LootProvider<O> provider,
                                     @NotNull Consumer<ItemStack> consumer) {
        if (provider == null) {
            plugin.getLogger().severe("[Loot] LootProvider was null for object: " + object);
            return;
        }

        List<ItemStack> lootList;
        try {
            lootList = provider.createLoot(plugin, player, object);
        } catch (Exception e) {
            plugin.getLogger().severe("[Loot] Exception in LootProvider.createLoot for: "
                    + object + " | " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (lootList == null) {
            plugin.getLogger().warning("[Loot] LootProvider.createLoot returned null for: " + object);
            return;
        }

        for (ItemStack item : lootList) {
            if (item != null && item.getType() != Material.AIR) {
                try {
                    consumer.accept(item);
                } catch (Exception e) {
                    plugin.getLogger().severe("[Loot] Exception while consuming item for: "
                            + object + " | " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                plugin.getLogger().warning("[Loot] Generated null or AIR item for: " + object);
            }
        }
    }

    /**
     * Drops loot at a centered location around the passed-in location.
     */
    private static <O> void dropLoot(@NotNull LootConomyPlugin plugin,
                                     @NotNull Player player,
                                     @NotNull O object,
                                     @NotNull LootProvider<O> provider,
                                     @NotNull Location location) {
        Location center = LocationUtil.setCenter3D(location.clone());
        dropLoot(plugin, player, object, provider, itemStack -> {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                player.getWorld().dropItem(center, itemStack);
            }
        });
    }

    /**
     * Handler for when a block is broken.
     */
    public static final LootHandler<BlockBreakEvent, Material> BLOCK_BREAK = (plugin, event, provider) -> {
        Block block = event.getBlock();
        BlockData blockData = block.getBlockData();
        Material blockType = block.getType();
        boolean isTall = (blockType == Material.BAMBOO || blockType == Material.SUGAR_CANE);

        if (!isTall && blockData instanceof Ageable age) {
            if (age.getAge() < age.getMaximumAge()) {
                // Not fully grown → skip
                return false;
            }
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
                // Already processed by another plugin/tracker → skip
                continue;
            }

            // Simulate a sub-block break for multi-block crops/trees
            Location center = LocationUtil.setCenter3D(block.getLocation());

            dropLoot(plugin, player, blockType, provider, itemStack -> {
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    plugin.getLogger().warning("[Loot][BLOCK_BREAK] Null/AIR for block type: " + blockType);
                    return;
                }

                if (ExcellentEnchantsHook.hasTelekinesis(tool)) {
                    plugin.getMoneyManager().pickupMoney(player, itemStack);
                } else {
                    player.getWorld().dropItem(center, itemStack);
                }
            });
        }

        return true;
    };

    /**
     * Handler for when a block is harvested (e.g., crops).
     */
    public static final LootHandler<PlayerHarvestBlockEvent, Material> BLOCK_HARVEST = (plugin, event, provider) -> {
        Block block = event.getHarvestedBlock();
        if (PlayerBlockTracker.isTracked(block)) {
            return false;
        }

        Player player = event.getPlayer();
        dropLoot(plugin, player, block.getType(), provider, itemStack -> {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                plugin.getLogger().warning("[Loot][BLOCK_HARVEST] Null/AIR for block type: " + block.getType());
                return;
            }
            event.getItemsHarvested().add(itemStack);
        });

        return true;
    };

    /**
     * Handler for when a living entity dies.
     */
    public static final LootHandler<EntityDeathEvent, EntityType> ENTITY_KILL = (plugin, event, provider) -> {
        LivingEntity entity = event.getEntity();
        if (entity == null) {
            plugin.getLogger().warning("[Loot][ENTITY_KILL] EntityDeathEvent.getEntity() returned null");
            return false;
        }

        Player killer = entity.getKiller();
        if (killer == null) {
            // Not killed by a player → skip
            return false;
        }

        // --- Debug Logging ---
        plugin.getLogger().info("[Loot][ENTITY_KILL] Processing death of " + entity.getType()
                + " (killed by: " + killer.getName() + ")");

        // — Check if entity is “devastated” (custom tag or state) —
        boolean isDevastated;
        try {
            isDevastated = MoneyUtils.isDevastated(entity);
        } catch (Exception e) {
            plugin.getLogger().severe("[Loot][ENTITY_KILL] isDevastated threw: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        if (isDevastated) {
            plugin.getLogger().info("[Loot][ENTITY_KILL] Skipping " + entity.getType() + " because it is devastated");
            return false;
        }

        // — Check if entity is vanilla (skip modded/custom mobs) —
        boolean isVanilla;
        try {
            isVanilla = MoneyUtils.isVanillaMob(entity);
        } catch (Exception e) {
            plugin.getLogger().severe("[Loot][ENTITY_KILL] isVanillaMob threw: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        if (!isVanilla) {
            plugin.getLogger().info("[Loot][ENTITY_KILL] Skipping " + entity.getType() + " because it is not a vanilla mob");
            return false;
        }

        // — Check vehicle (skip if riding a non-living entity) —
        Entity vehicle = entity.getVehicle();
        if (vehicle != null && !(vehicle instanceof LivingEntity)) {
            plugin.getLogger().info("[Loot][ENTITY_KILL] Skipping " + entity.getType()
                    + " because vehicle is non-living: " + vehicle.getType());
            return false;
        }

        // — Check last damage cause (skip if from cramming) —
        EntityDamageEvent lastCause = entity.getLastDamageCause();
        if (lastCause != null) {
            var ds = lastCause.getDamageSource();
            if (ds != null && ds.getDamageType() == DamageType.CRAMMING) {
                plugin.getLogger().info("[Loot][ENTITY_KILL] Skipping " + entity.getType()
                        + " due to cramming death");
                return false;
            }
        }

        // — Generate and add loot to event —
        try {
            plugin.getLogger().info("[Loot][ENTITY_KILL] Generating loot for " + entity.getType());
            dropLoot(plugin, killer, entity.getType(), provider, itemStack -> {
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    plugin.getLogger().warning("[Loot][ENTITY_KILL] Null/AIR loot for " + entity.getType());
                    return;
                }

                List<ItemStack> drops = event.getDrops();
                if (drops == null) {
                    plugin.getLogger().severe("[Loot][ENTITY_KILL] event.getDrops() returned null for "
                            + entity.getType());
                } else {
                    drops.add(itemStack);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("[Loot][ENTITY_KILL] Exception while processing loot for "
                    + entity.getType() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    };

    /**
     * Handler for when a player shears an entity (e.g., sheep).
     */
    public static final LootHandler<PlayerShearEntityEvent, EntityType> ENTITY_SHEAR = (plugin, event, provider) -> {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();
        if (entity == null) {
            plugin.getLogger().warning("[Loot][ENTITY_SHEAR] Event.getEntity() returned null");
            return false;
        }

        dropLoot(plugin, player, entity.getType(), provider, entity.getLocation());
        return true;
    };

    /**
     * Handler for when a player catches an item via fishing.
     */
    public static final LootHandler<PlayerFishEvent, Material> ITEM_FISH = (plugin, event, provider) -> {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return false;
        }

        Entity caught = event.getCaught();
        if (!(caught instanceof Item item)) {
            return false;
        }

        Player player = event.getPlayer();
        dropLoot(plugin, player, item.getItemStack().getType(), provider, itemStack -> {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                plugin.getLogger().warning("[Loot][ITEM_FISH] Null/AIR loot for caught item");
                return;
            }

            Location locHook = event.getHook().getLocation();
            Location locPlayer = player.getLocation();
            Vector vec3d = new Vector(
                    locPlayer.getX() - locHook.getX(),
                    locPlayer.getY() - locHook.getY(),
                    locPlayer.getZ() - locHook.getZ()
            ).multiply(0.1D);

            Item drop = player.getWorld().dropItem(caught.getLocation(), itemStack);
            drop.setVelocity(drop.getVelocity().add(vec3d));
        });

        return true;
    };
}
