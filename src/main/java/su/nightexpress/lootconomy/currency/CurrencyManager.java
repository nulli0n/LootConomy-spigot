package su.nightexpress.lootconomy.currency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;

import java.util.*;

public class CurrencyManager extends AbstractManager<LootConomyPlugin> {

    private static final String FILE_NAME = "currencies.yml";

    private final FileConfig                    config;
    private final Map<String, CurrencySettings> settingsMap;

    public CurrencyManager(@NotNull LootConomyPlugin plugin) {
        super(plugin);
        this.config = FileConfig.loadOrExtract(plugin, FILE_NAME);
        this.settingsMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.loadSettings();

        this.config.saveChanges();
    }

    private void loadSettings() {
        EconomyBridge.getCurrencies().forEach(currency -> {
            CurrencySettings settings = this.readSettings(currency);
            this.settingsMap.put(currency.getInternalId(), settings);
        });
        this.plugin.info("Loaded " + this.settingsMap.size() + " currency settings.");
    }

    @Override
    protected void onShutdown() {
        this.settingsMap.clear();
    }

    @NotNull
    public CurrencySettings readSettings(@NotNull Currency currency) {
        return CurrencySettings.read(this.config, currency.getInternalId(), currency);
    }

//    public void registerCurrency(@NotNull String id, @NotNull Supplier<CurrencyHandler> supplier) {
//        CurrencyHandler handler = supplier.get();
//        CurrencySettings settings = this.readSettings(id, handler);
//        this.registerCurrency(new StandardCurrency<>(id, handler, settings));
//    }
//
//    public void registerCurrency(@NotNull Currency currency) {
//        this.currencyMap.put(currency.getId(), currency);
//        this.plugin.info("Registered currency: '" + currency.getId() + "'.");
//    }

//    public boolean isRegistered(@NotNull String id) {
//        return this.getCurrency(id) != null;
//    }
//
//    public boolean hasCurrency() {
//        return !this.currencyMap.isEmpty();
//    }

//    @NotNull
//    public Map<String, Currency> getCurrencyMap() {
//        return Collections.unmodifiableMap(this.currencyMap);
//    }
//
//    @NotNull
//    public Set<Currency> getCurrencies() {
//        return new HashSet<>(this.currencyMap.values());
//    }
//
//    @NotNull
//    public List<String> getCurrencyIds() {
//        return new ArrayList<>(this.currencyMap.keySet());
//    }

    @Nullable
    public CurrencySettings getSettings(@NotNull Currency currency) {
        return this.getSettings(currency.getInternalId());
    }

    @Nullable
    public CurrencySettings getSettings(@NotNull String id) {
        return this.settingsMap.get(id.toLowerCase());
    }
}
