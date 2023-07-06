package su.nightexpress.lootconomy.currency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.api.currency.CurrencyHandler;
import su.nightexpress.lootconomy.currency.handler.CoinsEngineHandler;
import su.nightexpress.lootconomy.currency.handler.GamePointsHandler;
import su.nightexpress.lootconomy.currency.handler.VaultEconomyHandler;
import su.nightexpress.lootconomy.currency.impl.CoinsEngineCurrency;
import su.nightexpress.lootconomy.currency.impl.ConfigCurrency;
import su.nightexpress.lootconomy.hook.HookId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CurrencyManager extends AbstractManager<LootConomy> {

    public static final String DIR_CURRENCIES = "/currency/";

    private final Map<String, Currency> currencyMap;

    public CurrencyManager(@NotNull LootConomy plugin) {
        super(plugin);
        this.currencyMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.plugin.getConfigManager().extractResources(DIR_CURRENCIES);

        if (Hooks.hasVault() && VaultHook.hasEconomy()) {
            this.registerCurrency(Hooks.VAULT, VaultEconomyHandler::new);
        }
        if (Hooks.hasPlugin(HookId.GAME_POINTS)) {
            this.registerCurrency(HookId.GAME_POINTS, GamePointsHandler::new);
            this.deprecatedCurrency(HookId.GAME_POINTS);
        }
        if (Hooks.hasPlugin(HookId.COINS_ENGINE)) {
            CoinsEngineAPI.getCurrencyManager().getCurrencies().forEach(cura -> {
                if (!cura.isVaultEconomy()) {
                    String id = "coinsengine_" + cura.getId();
                    JYML config = this.getConfig(id);
                    this.registerCurrency(new CoinsEngineCurrency(plugin, config, new CoinsEngineHandler(cura)));
                }
            });
        }
    }

    private void deprecatedCurrency(@NotNull String plugin) {
        this.plugin.warn("=".repeat(15));
        this.plugin.warn("Support for the '" + plugin + "' plugin is deprecated!");
        this.plugin.warn("Please, consider to switch to our new free custom currency & economy " + HookId.COINS_ENGINE + " plugin instead.");
        this.plugin.warn("=".repeat(15));
    }

    @Override
    protected void onShutdown() {
        this.currencyMap.clear();
    }

    @NotNull
    private JYML getConfig(@NotNull String id) {
        return JYML.loadOrExtract(plugin, DIR_CURRENCIES + id + ".yml");
    }

    public boolean registerCurrency(@NotNull String id, @NotNull Supplier<CurrencyHandler> supplier) {
        return this.registerCurrency(new ConfigCurrency(plugin, this.getConfig(id), supplier.get()));
    }

    public boolean registerCurrency(@NotNull Currency currency) {
        if (currency instanceof ConfigCurrency configCurrency) {
            if (!configCurrency.load()) {
                this.plugin.warn("Currency not loaded: '" + currency.getId() + "' !");
                return false;
            }
        }
        this.currencyMap.put(currency.getId(), currency);
        this.plugin.info("Registered currency: " + currency.getId());
        return true;
    }

    public boolean hasCurrency() {
        return !this.currencyMap.isEmpty();
    }

    @NotNull
    public Collection<Currency> getCurrencies() {
        return currencyMap.values();
    }

    @NotNull
    public Set<String> getCurrencyIds() {
        return this.currencyMap.keySet();
    }

    @Nullable
    public Currency getCurrency(@NotNull String id) {
        return this.currencyMap.get(id.toLowerCase());
    }

    @NotNull
    public Currency getAny() {
        return this.getCurrencies().stream().findFirst().orElseThrow();
    }
}
