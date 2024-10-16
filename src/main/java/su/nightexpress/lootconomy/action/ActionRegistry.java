package su.nightexpress.lootconomy.action;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.lootconomy.hook.impl.MythicMobsHook;
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.Plugins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ActionRegistry extends SimpleManager<LootConomyPlugin> {

    private static final Map<String, ActionType<?, ?>> ACTION_TYPE_MAP = new HashMap<>();

    public ActionRegistry(@NotNull LootConomyPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        // Block Material related
        this.registerAction(BlockBreakEvent.class, EventPriority.HIGHEST, ActionTypes.BLOCK_BREAK);
        this.registerAction(PlayerHarvestBlockEvent.class, EventPriority.MONITOR, ActionTypes.BLOCK_HARVEST);

        // Entity Type related
        this.registerAction(EntityDeathEvent.class, EventPriority.MONITOR, ActionTypes.ENTITY_KILL);
        this.registerAction(EntityDeathEvent.class, EventPriority.MONITOR, ActionTypes.ENTITY_SHOOT);
        this.registerAction(PlayerShearEntityEvent.class, EventPriority.MONITOR, ActionTypes.ENTITY_SHEAR);

        // Item Material related
        this.registerAction(PlayerFishEvent.class, EventPriority.MONITOR, ActionTypes.ITEM_FISH);

        this.registerHooks();
    }

    private void registerHooks() {
        this.registerExternal(HookId.MYTHIC_MOBS, MythicMobsHook::register);
    }

    private void registerExternal(@NotNull String plugin, @NotNull Consumer<ActionRegistry> consumer) {
        if (Plugins.isLoaded(plugin)) {
            this.plugin.info("Found " + plugin + "! Registering new objective types...");
            consumer.accept(this);
        }
    }

    @Override
    protected void onShutdown() {
        ACTION_TYPE_MAP.clear();
    }

    @Nullable
    public <E extends Event, O> ActionType<E, O> registerAction(@NotNull Class<E> eventClass,
                                                                @NotNull EventPriority priority,
                                                                @NotNull String name,
                                                                @NotNull ObjectFormatter<O> objectFormatter,
                                                                @NotNull EventHelper<E, O> dataGather) {
        return this.registerAction(eventClass, priority, ActionType.create(name, objectFormatter, dataGather));
    }

    @Nullable
    public <E extends Event, O> ActionType<E, O> registerAction(@NotNull Class<E> eventClass,
                                                                @NotNull EventPriority priority,
                                                                @NotNull ActionType<E, O> actionType) {

        if (!actionType.loadSettings(this.plugin.getConfig())) return null;

        WrappedEvent<E, O> event = new WrappedEvent<>(plugin, eventClass, actionType);
        plugin.getPluginManager().registerEvent(eventClass, event, priority, event, plugin, true);

        ACTION_TYPE_MAP.put(actionType.getName(), actionType);
        return actionType;
    }

    @Nullable
    public ActionType<?, ?> getActionType(@NotNull String name) {
        return ACTION_TYPE_MAP.get(name.toLowerCase());
    }

    @NotNull
    public Set<ActionType<?, ?>> getActionTypes() {
        return new HashSet<>(ACTION_TYPE_MAP.values());
    }
}
