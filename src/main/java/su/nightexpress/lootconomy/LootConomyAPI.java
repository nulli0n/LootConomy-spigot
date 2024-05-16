package su.nightexpress.lootconomy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.money.MoneyManager;

public class LootConomyAPI {

    public static LootConomyPlugin plugin;

    public static void setup(@NotNull LootConomyPlugin plugin) {
        LootConomyAPI.plugin = plugin;
    }

    @NotNull
    public static LootUser getUserData(@NotNull Player player) {
        return plugin.getUserManager().getUserData(player);
    }

    @Nullable
    public static Currency getCurrency(@NotNull String id) {
        return plugin.getCurrencyManager().getCurrency(id);
    }

    @NotNull
    public static MoneyManager getMoneyManager() {
        return plugin.getMoneyManager();
    }
}
