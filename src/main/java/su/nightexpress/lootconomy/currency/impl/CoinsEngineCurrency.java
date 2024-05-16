package su.nightexpress.lootconomy.currency.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.lootconomy.currency.CurrencyManager;
import su.nightexpress.lootconomy.currency.CurrencySettings;
import su.nightexpress.lootconomy.currency.handler.CoinsEngineHandler;
import su.nightexpress.lootconomy.hook.HookId;

public class CoinsEngineCurrency extends StandardCurrency<CoinsEngineHandler> {

    public CoinsEngineCurrency(@NotNull String id, @NotNull CoinsEngineHandler handler, @NotNull CurrencySettings settings) {
        super(id, handler, settings);

        this.placeholderMap.clear();
        this.placeholderMap.add(handler.getCurrency().getPlaceholders());
    }

    public static void register(@NotNull CurrencyManager currencyManager) {
        CoinsEngineAPI.getCurrencyManager().getCurrencies().forEach(currency -> {
            if (!currency.isVaultEconomy()) {
                String id = HookId.COINS_ENGINE.toLowerCase() + "_" + currency.getId();
                CoinsEngineHandler handler = new CoinsEngineHandler(currency);
                CurrencySettings settings = currencyManager.readSettings(id, handler);

                currencyManager.registerCurrency(new CoinsEngineCurrency(id, handler, settings));
            }
        });
    }

    @Override
    @NotNull
    public String formatValue(double price) {
        return this.handler.getCurrency().formatValue(price);
    }

    @Override
    @NotNull
    public String format(double amount) {
        return this.handler.getCurrency().format(amount);
    }

    @Override
    public double round(double amount) {
        return this.handler.getCurrency().fine(super.round(amount));
    }

    @Override
    @NotNull
    public String dropFormat(double amount) {
        return this.handler.getCurrency().formatCompact(amount);
    }

    @Override
    @NotNull
    public String getName() {
        return this.handler.getCurrency().getName();
    }

    @Override
    @NotNull
    public String getFormat() {
        return this.handler.getCurrency().getFormat();
    }

    @Override
    @NotNull
    public String getDropFormat() {
        return this.handler.getCurrency().getFormatShort();
    }

    @Override
    @NotNull
    public ItemStack getIcon(double amount) {
        return this.handler.getCurrency().getIcon();
    }
}
