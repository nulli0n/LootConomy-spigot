package su.nightexpress.lootconomy.currency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.api.currency.CurrencyHandler;
import su.nightexpress.lootconomy.currency.handler.VaultEconomyHandler;
import su.nightexpress.lootconomy.currency.impl.CoinsEngineCurrency;
import su.nightexpress.lootconomy.currency.impl.StandardCurrency;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.VaultHook;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Plugins;

import java.util.*;
import java.util.function.Supplier;

public class CurrencyManager extends AbstractManager<LootConomyPlugin> {

    private static final String FILE_NAME = "currencies.yml";

    private final FileConfig config;
    private final Map<String, Currency> currencyMap;

    public CurrencyManager(@NotNull LootConomyPlugin plugin) {
        super(plugin);
        this.config = FileConfig.loadOrExtract(plugin, FILE_NAME);
        this.currencyMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.loadCurrencies();

        this.config.saveChanges();
    }

    private void loadCurrencies() {
        if (Plugins.hasVault() && VaultHook.hasEconomy()) {
            this.registerCurrency(VaultEconomyHandler.ID, VaultEconomyHandler::new);
        }

        if (Plugins.isLoaded(HookId.COINS_ENGINE)) {
            CoinsEngineCurrency.register(this);
        }
    }

    @Override
    protected void onShutdown() {
        this.currencyMap.clear();
    }

    @NotNull
    public CurrencySettings readSettings(@NotNull String id, @NotNull CurrencyHandler handler) {
        return CurrencySettings.read(this.config, id, handler);
    }

    public void registerCurrency(@NotNull String id, @NotNull Supplier<CurrencyHandler> supplier) {
        CurrencyHandler handler = supplier.get();
        CurrencySettings settings = this.readSettings(id, handler);
        this.registerCurrency(new StandardCurrency<>(id, handler, settings));
    }

    public void registerCurrency(@NotNull Currency currency) {
        this.currencyMap.put(currency.getId(), currency);
        this.plugin.info("Registered currency: '" + currency.getId() + "'.");
    }

    public boolean isRegistered(@NotNull String id) {
        return this.getCurrency(id) != null;
    }

    public boolean hasCurrency() {
        return !this.currencyMap.isEmpty();
    }

    @NotNull
    public Map<String, Currency> getCurrencyMap() {
        return Collections.unmodifiableMap(this.currencyMap);
    }

    @NotNull
    public Set<Currency> getCurrencies() {
        return new HashSet<>(this.currencyMap.values());
    }

    @NotNull
    public List<String> getCurrencyIds() {
        return new ArrayList<>(this.currencyMap.keySet());
    }

    @Nullable
    public Currency getCurrency(@NotNull String id) {
        return this.currencyMap.get(id.toLowerCase());
    }
}
