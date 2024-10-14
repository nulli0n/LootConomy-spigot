package su.nightexpress.lootconomy.action;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import su.nightexpress.nightcore.util.ItemUtil;

public class ActionTypes {

    public static final ActionType<BlockBreakEvent, Material> BLOCK_BREAK = ActionType.create(
        "block_break", ObjectFormatters.MATERIAL, EventHelpers.BLOCK_BREAK
    ).setIcon(ItemUtil.getSkinHead("1e1d4bc469d29d22a7ef6d21a61b451291f21bf51fd167e7fd07b719512e87a1"));

    public static final ActionType<PlayerHarvestBlockEvent, Material> BLOCK_HARVEST = ActionType.create(
        "harvest_block", ObjectFormatters.MATERIAL, EventHelpers.BLOCK_HARVEST
    ).setIcon(ItemUtil.getSkinHead("dc856bd5b0f5fd20563bf68b1fe3a58723d3af9caef859221c7ecfa9642ff19b"));

    public static final ActionType<EntityDeathEvent, EntityType> ENTITY_KILL = ActionType.create(
        "kill_entity", ObjectFormatters.ENITITY_TYPE, EventHelpers.ENTITY_KILL
    ).setIcon(ItemUtil.getSkinHead("783aaaee22868cafdaa1f6f4a0e56b0fdb64cd0aeaabd6e83818c312ebe66437"));

    public static final ActionType<EntityDeathEvent, EntityType> ENTITY_SHOOT = ActionType.create(
        "shoot_entity", ObjectFormatters.ENITITY_TYPE, EventHelpers.ENTITY_SHOOT
    ).setIcon(ItemUtil.getSkinHead("c787b7afb5a59953975bba2473749b601d54d6f93ceac7a02ac69aae7f9b8"));

    public static final ActionType<PlayerShearEntityEvent, EntityType> ENTITY_SHEAR = ActionType.create(
        "shear_entity", ObjectFormatters.ENITITY_TYPE, EventHelpers.ENTITY_SHEAR
    ).setIcon(ItemUtil.getSkinHead("a723893df4cfb9c7240fc47b560ccf6ddeb19da9183d33083f2c71f46dad290a"));

    public static final ActionType<PlayerFishEvent, Material> ITEM_FISH = ActionType.create(
        "fish_item", ObjectFormatters.MATERIAL, EventHelpers.ITEM_FISH
    ).setIcon(ItemUtil.getSkinHead("1352df85a02d7fac5dca72dfbc6ba8ac0a7f96208bc8048247791ef2216f5c94"));
}
