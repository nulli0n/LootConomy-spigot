package su.nightexpress.lootconomy.currency.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.currency.handler.CoinsEngineHandler;

public class CoinsEngineCurrency extends ConfigCurrency {

    public CoinsEngineCurrency(@NotNull LootConomy plugin, @NotNull JYML cfg, @NotNull CoinsEngineHandler handler) {
        super(plugin, cfg, handler);
    }

    @Override
    @NotNull
    public CoinsEngineHandler getHandler() {
        return (CoinsEngineHandler) super.getHandler();
    }

    @Override
    @NotNull
    public String formatValue(double amount) {
        return this.getHandler().getCurrency().formatValue(amount);
    }

    @Override
    @NotNull
    public String format(double amount) {
        return this.getHandler().getCurrency().format(amount);
    }

    @Override
    public double round(double amount) {
        return this.getHandler().getCurrency().fine(amount);
    }
}
