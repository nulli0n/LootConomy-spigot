package su.nightexpress.lootconomy.api.currency;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.nightcore.util.text.tag.Tags;

public interface CurrencyHandler {

    @NotNull String getDefaultName();

    @NotNull
    default String getDefaultFormat() {
        return Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_NAME;
    }

    @NotNull
    default String getDefaultDropFormat() {
        return Tags.YELLOW.enclose(Placeholders.GENERIC_AMOUNT + " " + Placeholders.GENERIC_NAME);
    }

    @NotNull ItemStack getDefaultIcon();

    double getBalance(@NotNull Player player);

    void give(@NotNull Player player, double amount);

    void take(@NotNull Player player, double amount);
}
