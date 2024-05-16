package su.nightexpress.lootconomy.action;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;

public interface EventHelper <E extends Event, O> {

    boolean handle(@NotNull LootConomyPlugin plugin, @NotNull E event, @NotNull LootProcessor<O> processor);
}
