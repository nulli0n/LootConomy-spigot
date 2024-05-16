package su.nightexpress.lootconomy.action;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.money.MoneyUtils;

import java.util.ArrayList;
import java.util.List;

public class WrappedEvent <E extends Event, O> implements Listener, EventExecutor, LootProcessor<O> {

    private final LootConomyPlugin plugin;
    private final Class<E>         eventClass;
    private final ActionType<E, O> actionType;

    public WrappedEvent(@NotNull LootConomyPlugin plugin,
                        @NotNull Class<E> eventClass,
                        @NotNull ActionType<E, O> actionType) {
        this.plugin = plugin;
        this.eventClass = eventClass;
        this.actionType = actionType;
    }

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event bukkitEvent) {
        if (!this.eventClass.isAssignableFrom(bukkitEvent.getClass())) return;

        E event = this.eventClass.cast(bukkitEvent);
        this.actionType.getEventHelper().handle(this.plugin, event, this);
    }

    @Override
    @NotNull
    public List<ItemStack> getLoot(@NotNull Player player, @NotNull O object, int amount) {
        if (!MoneyUtils.isMoneyAvailable(player)) return new ArrayList<>();

        return this.plugin.getMoneyManager().createLoot(player, this.actionType, object, amount);
    }
}
