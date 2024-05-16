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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.lootconomy.hook.impl.MythicMobsHook;
import su.nightexpress.lootconomy.money.MoneyUtils;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.blocktracker.PlayerBlockTracker;

public class EventHelpers {

    /*private static <O> void dropLoot(@NotNull Player player, @NotNull O object, @NotNull LootProcessor<O> processor, @NotNull Location location) {
        dropLoot(player, object, 1, processor, location);
    }*/

    private static <O> void dropLoot(@NotNull Player player, @NotNull O object/*, int amount*/, @NotNull LootProcessor<O> processor, @NotNull Location location) {
        Location center = LocationUtil.getCenter(location.clone());
        processor.getLoot(player, object, 1).forEach(itemStack -> {
            player.getWorld().dropItem(center, itemStack);
        });
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
        //ItemStack tool = player.getInventory().getItemInMainHand();
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
            processor.getLoot(player, blockType, 1).forEach(itemStack -> {
                dropLoot(player, blockType, processor, event2.getBlock().getLocation());
                /*if (EngineUtils.hasPlugin(HookId.EXCELLENT_ENCHANTS) && ExcellentEnchantsHook.hasTelekinesis(tool)) {
                    this.plugin.getMoneyManager().pickupMoney(player, item);
                    return;
                }*/
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
        /*processor.getLoot(player, block.getType(), 1).forEach(itemStack -> {
            Location location = LocationUtil.getCenter(block.getLocation(), false);
            block.getWorld().dropItem(location, itemStack);
        });*/
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

    /*public static final EventHelper<BlockPlaceEvent, Material> BLOCK_PLACE = (plugin, event, processor) -> {
        Block block = event.getBlockPlaced();

        processor.getLoot(event.getPlayer(), block.getType(), 1);
        return false;
    };

    public static final EventHelper<EntityBreedEvent, EntityType> ENTITY_BREED = (plugin, event, processor) -> {
        LivingEntity breeder = event.getBreeder();
        if (!(breeder instanceof Player player)) return false;

        Entity entity = event.getEntity();

        dropLoot(player, entity.getType(), processor, entity.getLocation());
        return true;
    };*/

    public static final EventHelper<EntityDeathEvent, EntityType> ENTITY_KILL = (plugin, event, processor) -> {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return false;
        if (MoneyUtils.isDevastated(entity)) return false;
        if (Plugins.isLoaded(HookId.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(entity)) return false;
        if (entity.getVehicle() instanceof Minecart || entity.getVehicle() instanceof Boat) return false;
        if (entity.getLastDamageCause() != null && entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.CRAMMING) return false;

        Player killer = entity.getKiller();
        if (killer == null) return false;

        //ItemStack tool = killer.getInventory().getItemInMainHand();

        processor.getLoot(killer, entity.getType(), 1).forEach(itemStack -> {
            /*if (Plugins.isLoaded(HookId.EXCELLENT_ENCHANTS) && ExcellentEnchantsHook.hasNimble(tool)) {
                plugin.getMoneyManager().pickupMoney(killer, item);
                return;
            }*/
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

        processor.getLoot(killer, entity.getType(), 1).forEach(itemStack -> {
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

    /*public static final EventHelper<EntityTameEvent, EntityType> ENTITY_TAME = (plugin, event, processor) -> {
        Player player = (Player) event.getOwner();
        LivingEntity entity = event.getEntity();

        dropLoot(player, entity.getType(), processor, entity.getLocation());
        return true;
    };

    public static final EventHelper<CraftItemEvent, Material> ITEM_CRAFT = (plugin, event, processor) -> {
        if (event.getClick() == ClickType.MIDDLE) return false;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return false;

        Player player = (Player) event.getWhoClicked();
        ItemStack craft = new ItemStack(item);
        Material type = craft.getType();

        boolean numberKey = event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD;

        if (event.isShiftClick() || numberKey) {
            int has = Players.countItem(player, craft);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                int now = Players.countItem(player, craft);
                int crafted = now - has;
                dropLoot(player, type, crafted, processor, player.getLocation());
            });
        }
        else {
            ItemStack cursor = event.getCursor();
            if (cursor != null && !cursor.getType().isAir() && (!cursor.isSimilar(craft) || cursor.getAmount() >= cursor.getMaxStackSize()))
                return false;

            dropLoot(player, type, processor, player.getLocation());
        }
        return true;
    };

    public static final EventHelper<InventoryClickEvent, Material> ITEM_DISENCHANT = (plugin, event, processor) -> {
        Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return false;
        if (event.getRawSlot() != 2 || event.getClick() == ClickType.MIDDLE) return false;

        ItemStack result = inventory.getItem(2);
        if (result == null || result.getType().isAir()) return false;

        ItemStack source = inventory.getItem(0);
        if (source == null || result.getType().isAir()) return false;

        if (source.getEnchantments().size() == result.getEnchantments().size()) return false;

        Player player = (Player) event.getWhoClicked();
        Location location = inventory.getLocation() == null ? player.getLocation() : inventory.getLocation();
        dropLoot(player, result.getType(), processor, location);
        return true;
    };

    public static final EventHelper<EnchantItemEvent, Material> ITEM_ENCHANT = (plugin, event, processor) -> {
        Player player = event.getEnchanter();
        ItemStack item = event.getItem();
        Inventory inventory = event.getInventory();
        Location location = inventory.getLocation() == null ? player.getLocation() : inventory.getLocation();

        processor.getLoot(player, item.getType(), 1);
        dropLoot(player, item.getType(), processor, location);
        return true;
    };*/

    public static final EventHelper<PlayerFishEvent, Material> ITEM_FISH = (plugin, event, processor) -> {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;

        Entity caught = event.getCaught();
        if (!(caught instanceof Item item)) return false;

        Player player = event.getPlayer();
        processor.getLoot(player, item.getItemStack().getType(), 1).forEach(itemStack -> {
            Location locHook = event.getHook().getLocation();
            Location locPlayer = player.getLocation();

            Vector vec3d = (new Vector(locPlayer.getX() - locHook.getX(), locPlayer.getY() - locHook.getY(), locPlayer.getZ() - locHook.getZ())).multiply(0.1D);
            Item drop = player.getWorld().dropItem(caught.getLocation(), itemStack);
            drop.setVelocity(drop.getVelocity().add(vec3d));
        });
        return true;
    };

    /*public static final EventHelper<FurnaceExtractEvent, Material> ITEM_FURNACE = (plugin, event, processor) -> {
        Player player = event.getPlayer();

        Material material = event.getItemType();
        int amount = event.getItemAmount();

        dropLoot(player, material, amount, processor, event.getBlock().getLocation());
        return true;
    };

    public static final EventHelper<BrewEvent, PotionEffectType> POTION_BREW = (plugin, event, processor) -> {
        BrewerInventory inventory = event.getContents();

        BrewingStand stand = inventory.getHolder();
        if (stand == null) return false;

        String uuidRaw = PDCUtil.getString(stand, Keys.brewingHolder).orElse(null);
        UUID uuid = uuidRaw == null ? null : UUID.fromString(uuidRaw);
        if (uuid == null) return false;

        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return false;

        int[] slots = new int[]{0, 1, 2};

        plugin.runTask(task -> {
            for (int slot : slots) {
                ItemStack item = inventory.getItem(slot);
                if (item == null || item.getType().isAir()) continue;

                ItemMeta meta = item.getItemMeta();
                if (!(meta instanceof PotionMeta potionMeta)) continue;

                PotionType potionType;
                if (Version.isAtLeast(Version.V1_20_R2)) {
                    for (PotionEffect effect : potionMeta.getBasePotionType().getPotionEffects()) {
                        dropLoot(player, effect.getType(), processor, stand.getLocation());
                    }
                }
                else {
                    potionType = potionMeta.getBasePotionData().getType();
                    if (potionType.getEffectType() != null) {
                        dropLoot(player, potionType.getEffectType(), processor, stand.getLocation());
                    }
                }

                potionMeta.getCustomEffects().forEach(effect -> {
                    dropLoot(player, effect.getType(), processor, stand.getLocation());
                });
            }
        });
        return true;
    };*/
}
