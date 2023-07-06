package su.nightexpress.lootconomy.booster.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.lootconomy.LootConomy;

public class BoosterUpdateTask extends AbstractTask<LootConomy> {

    //private long count = 0;

    public BoosterUpdateTask(@NotNull LootConomy plugin) {
        super(plugin, 30, true);
    }

    @Override
    public void action() {
        plugin.getBoosterManager().updateGlobal();

        /*if (Config.BOOSTERS_NOTIFY_INTERVAL > 0 && this.count / 60 % Config.BOOSTERS_NOTIFY_INTERVAL == 0) {
            plugin.getBoosterManager().notifyBooster();
        }
        if (++this.count >= Integer.MAX_VALUE) {
            this.count = 0;
        }*/
    }
}
