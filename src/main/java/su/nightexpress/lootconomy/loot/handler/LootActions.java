package su.nightexpress.lootconomy.loot.handler;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.loot.LootFormatter;
import su.nightexpress.lootconomy.api.loot.LootHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LootActions {

    private static final Map<String, LootAction<?, ?>> BY_ID = new HashMap<>();

    public static final LootAction<BlockBreakEvent, Material>          MINING       = new LootAction<>("block_break", "blocks", LootFormatters.MATERIAL, LootHandlers.BLOCK_BREAK);
    public static final LootAction<PlayerHarvestBlockEvent, Material>  GATHERING    = new LootAction<>("harvest_block", "gathering", LootFormatters.MATERIAL, LootHandlers.BLOCK_HARVEST);
    public static final LootAction<EntityDeathEvent, EntityType>       MOB_KILL     = new LootAction<>("kill_entity", "mobs", LootFormatters.ENITITY_TYPE, LootHandlers.ENTITY_KILL);
    public static final LootAction<PlayerShearEntityEvent, EntityType> SHEARING     = new LootAction<>("shear_entity", "shearing", LootFormatters.ENITITY_TYPE, LootHandlers.ENTITY_SHEAR);
    public static final LootAction<PlayerFishEvent, Material>          FISHING      = new LootAction<>("fish_item", "fishing", LootFormatters.MATERIAL, LootHandlers.ITEM_FISH);

    public static void registerDefaults() {
        register(MINING);
        register(GATHERING);
        register(MOB_KILL);
        register(SHEARING);
        register(FISHING);

        // Compatibility for old version configs.
        register(new LootAction<>("shoot_entity", "mobs", LootFormatters.ENITITY_TYPE, LootHandlers.ENTITY_KILL));
    }

    public static void clear() {
        BY_ID.clear();
    }

    @NotNull
    public static <E extends Event, O> LootAction<E, O> register(@NotNull String id,
                                                                 @NotNull String defCategory,
                                                                 @NotNull LootFormatter<O> formatter,
                                                                 @NotNull LootHandler<E, O> handler) {
        return register(new LootAction<>(id, defCategory, formatter, handler));
    }

    @NotNull
    public static <E extends Event, O> LootAction<E, O> register(@NotNull LootAction<E, O> action) {
        BY_ID.put(action.getId().toLowerCase(), action);
        return action;
    }

    public static boolean isPresent(@NotNull String name) {
        return getByName(name) != null;
    }

    @Nullable
    public static LootAction<?, ?> getByName(@NotNull String name) {
        return BY_ID.get(name.toLowerCase());
    }

    @NotNull
    public static Set<LootAction<?, ?>> getValues() {
        return new HashSet<>(BY_ID.values());
    }
}
