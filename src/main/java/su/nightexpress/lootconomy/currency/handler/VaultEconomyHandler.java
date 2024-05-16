package su.nightexpress.lootconomy.currency.handler;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.CurrencyHandler;
import su.nightexpress.nightcore.integration.VaultHook;
import su.nightexpress.nightcore.util.text.tag.Tags;

public class VaultEconomyHandler implements CurrencyHandler {

    public static final String ID = "economy";

    @Override
    @NotNull
    public String getDefaultName() {
        return "Money";
    }

    @Override
    @NotNull
    public String getDefaultFormat() {
        return "$" + Placeholders.GENERIC_AMOUNT;
    }

    @Override
    @NotNull
    public String getDefaultDropFormat() {
        return Tags.YELLOW.enclose(Tags.BOLD.enclose("$" + Placeholders.GENERIC_AMOUNT));
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return new ItemStack(Material.GOLD_NUGGET);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return VaultHook.getBalance(player);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        VaultHook.addMoney(player, amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        VaultHook.takeMoney(player, amount);
    }
}
