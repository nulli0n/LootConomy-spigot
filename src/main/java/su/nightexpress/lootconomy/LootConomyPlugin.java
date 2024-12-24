package su.nightexpress.lootconomy;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.booster.BoosterManager;
import su.nightexpress.lootconomy.command.impl.BaseCommands;
import su.nightexpress.lootconomy.command.impl.BoosterCommands;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Keys;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.currency.CurrencyManager;
import su.nightexpress.lootconomy.data.DataHandler;
import su.nightexpress.lootconomy.data.UserManager;
import su.nightexpress.lootconomy.hook.impl.PlaceholderHook;
import su.nightexpress.lootconomy.loot.handler.LootActions;
import su.nightexpress.lootconomy.money.MoneyManager;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.command.experimental.ImprovedCommands;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.blocktracker.PlayerBlockTracker;

public class LootConomyPlugin extends NightPlugin implements ImprovedCommands {

    private DataHandler dataHandler;
    private UserManager userManager;

    private CurrencyManager currencyManager;
    private BoosterManager  boosterManager;
    private MoneyManager    moneyManager;

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("LootConomy", new String[]{"lootconomy", "lc"})
            .setConfigClass(Config.class)
            .setLangClass(Lang.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    public void enable() {
        this.loadAPI();

        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.setup();

        PlayerBlockTracker.initialize();
        PlayerBlockTracker.BLOCK_FILTERS.add(block -> true);

        this.loadCommands();

        this.dataHandler = new DataHandler(this);
        this.dataHandler.setup();

        this.userManager = new UserManager(this, this.dataHandler);
        this.userManager.setup();

        this.boosterManager = new BoosterManager(this);
        this.boosterManager.setup();

        this.moneyManager = new MoneyManager(this);
        this.moneyManager.setup();

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }
    }

    @Override
    public void disable() {
        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }
        if (this.boosterManager != null) this.boosterManager.shutdown();
        if (this.moneyManager != null) this.moneyManager.shutdown();
        if (this.currencyManager != null) this.currencyManager.shutdown();
        if (this.userManager != null) this.userManager.shutdown();
        if (this.dataHandler != null) this.dataHandler.shutdown();

        LootActions.clear();
        LootConomyAPI.clear();
    }

    private void loadAPI() {
        LootConomyAPI.setup(this);
        Keys.load(this);
    }

    private void loadCommands() {
        BaseCommands.load(this);
        BoosterCommands.load(this);
    }

    @NotNull
    public DataHandler getDataHandler() {
        return this.dataHandler;
    }

    @NotNull
    public UserManager getUserManager() {
        return this.userManager;
    }

    @NotNull
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    @NotNull
    public BoosterManager getBoosterManager() {
        return boosterManager;
    }

    @NotNull
    public MoneyManager getMoneyManager() {
        return this.moneyManager;
    }
}
