package su.nightexpress.lootconomy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.money.MoneyManager;

public class LootConomyAPI {

    private static LootConomyPlugin plugin;

    static void setup(@NotNull LootConomyPlugin plugin) {
        LootConomyAPI.plugin = plugin;
    }

    static void clear() {
        plugin = null;
    }

    @NotNull
    public static LootConomyPlugin getPlugin() {
        return plugin;
    }

    @NotNull
    public static LootUser getUserData(@NotNull Player player) {
        return plugin.getUserManager().getOrFetch(player);
    }

    @NotNull
    public static MoneyManager getMoneyManager() {
        return plugin.getMoneyManager();
    }
}
