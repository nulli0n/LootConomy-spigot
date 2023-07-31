package su.nightexpress.lootconomy;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.command.list.ReloadSubCommand;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.playerblocktracker.PlayerBlockTracker;
import su.nightexpress.lootconomy.booster.BoosterManager;
import su.nightexpress.lootconomy.command.base.*;
import su.nightexpress.lootconomy.command.booster.BoosterCommand;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.currency.CurrencyManager;
import su.nightexpress.lootconomy.data.DataHandler;
import su.nightexpress.lootconomy.data.UserManager;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.hook.impl.PlaceholderHook;
import su.nightexpress.lootconomy.money.MoneyManager;
import su.nightexpress.lootconomy.skill.SkillManager;
import su.nightexpress.lootconomy.skill.impl.SkillType;

public class LootConomy extends NexPlugin<LootConomy> implements UserDataHolder<LootConomy, LootUser> {

    private DataHandler dataHandler;
    private UserManager userManager;

    private CurrencyManager    currencyManager;
    private BoosterManager     boosterManager;
    private SkillManager       skillManager;
    private MoneyManager       moneyManager;

    @Override
    @NotNull
    protected LootConomy getSelf() {
        return this;
    }

    @Override
    public void enable() {
        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.setup();
        if (!this.currencyManager.hasCurrency()) {
            this.error("No currencies are available! Plugin will be disabled.");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        this.boosterManager = new BoosterManager(this);
        this.boosterManager.setup();

        PlayerBlockTracker.initialize(this);

        this.skillManager = new SkillManager(this);
        this.skillManager.setup();

        this.moneyManager = new MoneyManager(this);
        this.moneyManager.setup();

        if (EngineUtils.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }
    }

    @Override
    public void disable() {
        if (EngineUtils.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }
        PlayerBlockTracker.shutdown();
        if (this.boosterManager != null) {
            this.boosterManager.shutdown();
            this.boosterManager = null;
        }
        if (this.skillManager != null) {
            this.skillManager.shutdown();
            this.skillManager = null;
        }
        if (this.moneyManager != null) {
            this.moneyManager.shutdown();
            this.moneyManager = null;
        }
        if (this.currencyManager != null) {
            this.currencyManager.shutdown();
            this.currencyManager = null;
        }
    }

    @Override
    public void loadConfig() {
        this.getConfig().initializeOptions(Config.class);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(Lang.class);
        this.getLangManager().loadEnum(SkillType.class);
        this.getLang().saveChanges();
    }

    @Override
    public boolean setupDataHandlers() {
        this.dataHandler = DataHandler.getInstance(this);
        this.dataHandler.setup();

        this.userManager = new UserManager(this);
        this.userManager.setup();

        return true;
    }

    @Override
    public void registerHooks() {

    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<LootConomy> mainCommand) {
        if (Config.GENERAL_DEDICATED_SKILLS_COMMAND_ENABLED.get()) {
            this.getCommandManager().registerCommand(new SkillsCommand(this, Config.GENERAL_DEDICATED_SKILLS_COMMAND_NAME.get()));
        }
        if (Config.LEVELING_ENABLED.get()) {
            mainCommand.addChildren(new StatsCommand(this));
            mainCommand.addChildren(new XPCommand(this));
            mainCommand.addChildren(new LevelCommand(this));
            mainCommand.addChildren(new ResetCommand(this));
            mainCommand.addChildren(new TopCommand(this));
        }
        mainCommand.addChildren(new ReloadSubCommand<>(this, Perms.COMMAND_RELOAD));
        mainCommand.addChildren(new InfoCommand(this));
        mainCommand.addChildren(new DropCommand(this));
        mainCommand.addChildren(new BoosterCommand(this));
        mainCommand.addChildren(new ObjectivesCommand(this));
        mainCommand.addChildren(new SkillsCommand(this, "skills"));
        mainCommand.addChildren(new SoundCommand(this));
    }

    @Override
    public void registerPermissions() {
        this.registerPermissions(Perms.class);
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
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    @NotNull
    public BoosterManager getBoosterManager() {
        return boosterManager;
    }

    @NotNull
    public SkillManager getSkillManager() {
        return this.skillManager;
    }

    @NotNull
    public MoneyManager getMoneyManager() {
        return this.moneyManager;
    }
}
