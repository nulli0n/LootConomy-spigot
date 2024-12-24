package su.nightexpress.lootconomy.hook.impl.mythicmobs;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.nightcore.manager.AbstractListener;

public class MythicMobsListener extends AbstractListener<LootConomyPlugin> {

    public MythicMobsListener(@NotNull LootConomyPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMobDeath(MythicMobDeathEvent event) {
        MythicMobsHook.ACTION.handle(plugin, event);
    }
}
