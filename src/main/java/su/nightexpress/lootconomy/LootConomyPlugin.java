package su.nightexpress.lootconomy;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.action.ActionRegistry;
import su.nightexpress.lootconomy.booster.BoosterManager;
import su.nightexpress.lootconomy.command.children.*;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Keys;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.currency.CurrencyManager;
import su.nightexpress.lootconomy.data.DataHandler;
import su.nightexpress.lootconomy.data.UserManager;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.hook.impl.PlaceholderHook;
import su.nightexpress.lootconomy.money.MoneyManager;
import su.nightexpress.nightcore.NightDataPlugin;
import su.nightexpress.nightcore.command.experimental.ImprovedCommands;
import su.nightexpress.nightcore.command.experimental.impl.ReloadCommand;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.blocktracker.PlayerBlockTracker;

public class LootConomyPlugin extends NightDataPlugin<LootUser> implements ImprovedCommands {

    private DataHandler dataHandler;
    private UserManager userManager;

    private ActionRegistry  actionRegistry;
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
        LootConomyAPI.setup(this);
        Keys.load(this);

        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.setup();
        if (!this.currencyManager.hasCurrency()) {
            this.error("No currencies are available! Plugin will be disabled.");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        PlayerBlockTracker.initialize();
        PlayerBlockTracker.BLOCK_FILTERS.add(block -> true);

        this.registerCommands();

        this.actionRegistry = new ActionRegistry(this);
        this.actionRegistry.setup();

        this.dataHandler = new DataHandler(this);
        this.dataHandler.setup();

        this.userManager = new UserManager(this);
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
        if (this.actionRegistry != null) this.actionRegistry.shutdown();
    }

    private void registerCommands() {
        ChainedNode rootNode = this.getRootNode();

        ReloadCommand.inject(this, rootNode, Perms.COMMAND_RELOAD);
        DropCommand.inject(this, rootNode);
        // InfoCommand
        ObjectivesCommand.inject(this, rootNode);
        SoundCommand.inject(this, rootNode);
        BoosterCommand.inject(this, rootNode);
        BoostsCommand.inject(this, rootNode);
    }

    @Override
    @NotNull
    public DataHandler getData() {
        return this.dataHandler;
    }

    @NotNull
    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @NotNull
    public ActionRegistry getActionRegistry() {
        return actionRegistry;
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
