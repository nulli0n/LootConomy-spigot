package su.nightexpress.lootconomy.skill.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.LocationUtil;

public interface LootHandler<E extends Event> {

    LootHandler<BlockBreakEvent> BLOCK_BREAK = (event, player, item) -> {
        Block block = event.getBlock();
        Location location = LocationUtil.getCenter(block.getLocation(), false);
        block.getWorld().dropItem(location, item);
    };

    LootHandler<EntityDeathEvent> KILL_MOB = (event, player, item) -> {
        event.getDrops().add(item);
    };

    LootHandler<PlayerFishEvent> FISHING = (event, player, item) -> {
        Entity caught = event.getCaught();
        if (caught == null) return;

        Location locHook = event.getHook().getLocation();
        Location locPlayer = player.getLocation();

        Vector vec3d = (new Vector(locPlayer.getX() - locHook.getX(), locPlayer.getY() - locHook.getY(), locPlayer.getZ() - locHook.getZ())).multiply(0.1D);
        Item drop = player.getWorld().dropItem(caught.getLocation(), item);
        drop.setVelocity(drop.getVelocity().add(vec3d));
    };

    void handleLoot(@NotNull E event, @NotNull Player player, @NotNull ItemStack item);
}
