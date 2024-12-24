package su.nightexpress.lootconomy.api.loot;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;

public interface LootHandler <E extends Event, O> {

    boolean handle(@NotNull LootConomyPlugin plugin, @NotNull E event, @NotNull LootProvider<O> provider);

}
