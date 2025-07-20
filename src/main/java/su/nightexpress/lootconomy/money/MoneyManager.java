package su.nightexpress.lootconomy.money;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.booster.BoosterUtils;
import su.nightexpress.lootconomy.currency.CurrencySettings;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.lootconomy.hook.impl.mythicmobs.MythicMobsHook;
import su.nightexpress.lootconomy.hook.impl.mythicmobs.MythicMobsListener;
import su.nightexpress.lootconomy.loot.handler.LootAction;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.lootconomy.api.event.PlayerCurrencyGainEvent;
import su.nightexpress.lootconomy.api.event.PlayerCurrencyLootCreateEvent;
import su.nightexpress.lootconomy.api.event.PlayerCurrencyLoseEvent;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.data.impl.LootLimitData;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.loot.handler.LootActions;
import su.nightexpress.lootconomy.loot.listener.LootListener;
import su.nightexpress.lootconomy.loot.objective.ObjectiveCategory;
import su.nightexpress.lootconomy.money.listener.AbuseListener;
import su.nightexpress.lootconomy.money.listener.MoneyItemListener;
import su.nightexpress.lootconomy.money.menu.CategoriesMenu;
import su.nightexpress.lootconomy.money.menu.ObjectivesMenu;
import su.nightexpress.lootconomy.money.object.DeathPenalty;
import su.nightexpress.lootconomy.money.object.DropInfo;
import su.nightexpress.lootconomy.money.object.MoneyObjective;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MoneyManager extends AbstractManager<LootConomyPlugin> {

    private final Map<String, ObjectiveCategory> categoryMap;
    private final Map<String, Map<String, MoneyObjective>> objectiveMap;

    private final Set<Item> trackedLoot;

    private CategoriesMenu categoriesMenu;
    private ObjectivesMenu objectivesMenu;

    public MoneyManager(@NotNull LootConomyPlugin plugin) {
        super(plugin);
        this.categoryMap = new HashMap<>();
        this.objectiveMap = new HashMap<>();
        this.trackedLoot = ConcurrentHashMap.newKeySet();
    }

    @Override
    protected void onLoad() {
        LootActions.registerDefaults();
        this.registerHooks();

        this.loadDefaults();
        this.loadCategories();
        this.loadObjectives();
        this.loadUI();

        this.addListener(new MoneyItemListener(this.plugin, this));
        this.addListener(new AbuseListener(this.plugin));
        this.addListener(new LootListener(this.plugin));

        if (Config.LOOT_MERGE_ENABLED.get()) {
            this.addTask(this::mergeMoneyItems, Config.LOOT_MERGE_INTERVAL.get());
        }
        if (Config.INVENTORY_SIZE_BYPASS_ENABLED.get()) {
            this.addTask(this::forcePickup, Config.INVENTORY_SIZE_BYPASS_INTERVAL.get());
        }
        this.addAsyncTask(this::playMoneyEffect, 5L);
    }

    @Override
    protected void onShutdown() {
        if (this.objectivesMenu != null) this.objectivesMenu.clear();
        if (this.categoriesMenu != null) this.categoriesMenu.clear();

        this.trackedLoot.clear();
        this.objectiveMap.clear();
        this.categoryMap.clear();
    }

    private void loadDefaults() {
        Creator creator = new Creator(this.plugin);
        creator.create();
    }

    private void registerHooks() {
        this.registerExternal(HookId.MYTHIC_MOBS, () -> {
            MythicMobsHook.register();
            return new MythicMobsListener(this.plugin);
        });
    }

    private void registerExternal(@NotNull String plugin, @NotNull Supplier<AbstractListener<LootConomyPlugin>> consumer) {
        if (Plugins.isLoaded(plugin)) {
            this.plugin.info("Found " + plugin + "! Registering new objective types...");
            this.addListener(consumer.get());
        }
    }

    private void loadCategories() {
        FileConfig config = this.plugin.getConfig();

        config.getSection("Objectives.Categories").forEach(sId -> {
            ObjectiveCategory category = ObjectiveCategory.read(config, "Objectives.Categories." + sId, sId);
            this.categoryMap.put(category.getId(), category);
        });

        this.plugin.info("Loaded " + this.categoryMap.size() + " categories.");
    }

    private void loadObjectives() {
        for (FileConfig config : FileConfig.loadAll(this.plugin.getDataFolder() + Config.DIR_OBJECTIVES)) {
            for (String object : config.getSection("")) {
                MoneyObjective objective = MoneyObjective.read(config, object, object);
                if (!this.validateObjective(objective, config)) continue;

                this.objectiveMap.computeIfAbsent(objective.getActionName(), k -> new HashMap<>()).put(objective.getId(), objective);
            }
            config.saveChanges();
        }
    }

    private boolean validateObjective(@NotNull MoneyObjective objective, @NotNull FileConfig config) {
        String id = objective.getId();
        String fileName = config.getFile().getName();

        var action = LootActions.getByName(objective.getActionName());
        if (action == null) {
            plugin.error("Invalid 'ActionType' for '" + id + "' objective (File: '" + fileName + "').");
            return false;
        }

        if (!this.hasCategory(objective.getCategory())) {
            plugin.warn("Invalid 'Category' for '" + id + "' objective (File: '" + fileName + "').");
        }

        objective.getObjects().forEach(objectId -> {
            if (action.parse(objectId) == null) {
                plugin.warn("Unknown object '" + objectId + "' in '" + id + "' objective (File: '" + fileName + "').");
            }
        });

        return true;
    }

    private void loadUI() {
        this.categoriesMenu = new CategoriesMenu(this.plugin);
        this.objectivesMenu = new ObjectivesMenu(this.plugin);
    }

    public void openObjectivesMenu(@NotNull Player player) {
        this.categoriesMenu.open(player);
    }

    public void openObjectivesMenu(@NotNull Player player, @NotNull ObjectiveCategory category) {
        this.objectivesMenu.open(player, category);
    }

    public boolean hasCategory(@NotNull String id) {
        return this.getCategoryById(id) != null;
    }

    @NotNull
    public Map<String, ObjectiveCategory> getCategoryMap() {
        return this.categoryMap;
    }

    @Nullable
    public ObjectiveCategory getCategoryById(@NotNull String id) {
        return this.categoryMap.get(id.toLowerCase());
    }

    @Nullable
    public ObjectiveCategory getCategoryByObjective(@NotNull MoneyObjective objective) {
        return this.getCategoryById(objective.getCategory());
    }

    @NotNull
    public Set<ObjectiveCategory> getCategories() {
        return new HashSet<>(this.categoryMap.values());
    }

    @NotNull
    public Map<String, Map<String, MoneyObjective>> getObjectiveMap() {
        return objectiveMap;
    }

    @NotNull
    public Map<String, MoneyObjective> getObjectivesMap(@NotNull LootAction<?, ?> actionType) {
        return this.objectiveMap.getOrDefault(actionType.getId(), Collections.emptyMap());
    }

    @NotNull
    public Collection<MoneyObjective> getObjectives(@NotNull LootAction<?, ?> actionType) {
        return this.getObjectivesMap(actionType).values();
    }

    @NotNull
    public Set<MoneyObjective> getObjectives() {
        Set<MoneyObjective> objectives = new HashSet<>();
        this.objectiveMap.values().forEach(map -> {
            objectives.addAll(map.values());
        });
        return objectives;
    }

    @NotNull
    public Set<MoneyObjective> getObjectives(@NotNull ObjectiveCategory category) {
        return this.getObjectives().stream().filter(objective -> objective.isCategory(category)).collect(Collectors.toSet());
    }

    @Nullable
    public MoneyObjective getObjective(@NotNull LootAction<?, ?> actionType, @NotNull String id) {
        return this.getObjectivesMap(actionType).get(id.toLowerCase());
    }

    @NotNull
    public Set<Item> getTrackedLoot() {
        this.trackedLoot.removeIf(item -> !item.isValid() || item.isDead());
        return trackedLoot;
    }

    @Nullable
    public Currency getCurrency(@NotNull ItemStack itemStack) {
        String currencyId = MoneyUtils.getCurrencyId(itemStack);
        if (currencyId == null) return null;

        return EconomyBridge.getCurrency(currencyId);
    }

    @Nullable
    public MoneyObjective getObjective(@NotNull ItemStack itemStack) {
        String objectiveId = MoneyUtils.getObjectiveId(itemStack);
        if (objectiveId == null) return null;

        String actionName = MoneyUtils.getActionName(itemStack);
        if (actionName == null) return null;

        var actionType = LootActions.getByName(actionName);
        if (actionType == null) return null;

        return this.getObjective(actionType, objectiveId);
    }

    public boolean pickupMoney(@NotNull Player player, @NotNull ItemStack itemStack) {
        Currency currency = this.getCurrency(itemStack);
        if (currency == null) return false;

        double money = MoneyUtils.getMoneyAmount(itemStack);

        return this.pickupMoney(player, currency, money, null);
    }

    public boolean pickupMoney(@NotNull Player player, @NotNull Currency currency, double money, @Nullable MoneyObjective objective) {
        boolean isLose = money < 0D;

        //money = currency.round(money);
        if (isLose) {
            return this.loseMoney(player, currency, money, objective);
        }
        else {
            return this.gainMoney(player, currency, money, objective);
        }
    }

    public boolean loseMoney(@NotNull Player player, @NotNull Currency currency, double money) {
        return this.loseMoney(player, currency, money, null);
    }

    public boolean loseMoney(@NotNull Player player, @NotNull Currency originCurrency, double originAmount, @Nullable MoneyObjective objective) {
        PlayerCurrencyLoseEvent event = new PlayerCurrencyLoseEvent(player, originCurrency, originAmount, objective);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        double amount = event.getAmount();
        Currency currency = event.getCurrency();
        currency.take(player, amount);

        (objective == null ? Lang.CURRENCY_LOST_DEATH : Lang.CURRENCY_LOST_PENALTY).getMessage().send(player, replacer -> {
            replacer
                .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
                .replace(Placeholders.GENERIC_BALANCE, currency.format(currency.getBalance(player)));

            if (objective != null) {
                replacer.replace(Placeholders.GENERIC_NAME, objective.getDisplayName());
            }
        });

//        if (objective != null) {
//            message = message.replace(Placeholders.GENERIC_NAME, objective.getDisplayName());
//        }
        //message.send(player);

        return true;
    }

    public boolean gainMoney(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.gainMoney(player, currency, amount, null);
    }

    public boolean gainMoney(@NotNull Player player, @NotNull Currency originCurrency, double originAmount, @Nullable MoneyObjective objective) {
        PlayerCurrencyGainEvent event = new PlayerCurrencyGainEvent(player, originCurrency, originAmount, objective);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        double amount = event.getAmount();
        Currency currency = event.getCurrency();
        currency.give(player, amount);

        CurrencySettings settings = plugin.getCurrencyManager().getSettings(currency);
        if (settings != null) {
            LootUser user = plugin.getUserManager().getOrFetch(player);
            if (user.getSettings().isPickupSound()) {
                settings.getPickupSound().play(player);
            }
        }

        Lang.CURRENCY_PICKUP.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .replace(Placeholders.GENERIC_BALANCE, currency.format(currency.getBalance(player)))
        );

        return true;
    }

//    @NotNull
//    @Deprecated
//    public <O> List<ItemStack> createLoot(@NotNull Player player, @NotNull LootAction<?, O> type, @NotNull O object) {
//        return this.createLoot(player, type, object, 1);
//    }
//
//    @NotNull
//    @Deprecated
//    public <O> List<ItemStack> createLoot(@NotNull Player player, @NotNull LootAction<?, O> type, @NotNull O object, int amount) {
//        List<ItemStack> loot = new ArrayList<>();
//        this.getObjectives(type).forEach(objective -> {
//            if (objective.hasObject(type.getObjectName(object))) {
//                loot.addAll(this.createLoot(player, objective, amount));
//            }
//        });
//        return loot;
//    }

    @NotNull
    public List<MoneyObjective> getObjectives(@NotNull LootAction<?, ?> action, @NotNull String objectId) {
        return this.getObjectives(action).stream().filter(objective -> objective.hasObject(objectId)).toList();
    }

    @NotNull
    public List<ItemStack> createLoot(@NotNull Player player, @NotNull MoneyObjective objective) {
        return this.createLoot(player, objective, 1);
    }

    @NotNull
    public List<ItemStack> createLoot(@NotNull Player player, @NotNull MoneyObjective objective, int instances) {
        List<ItemStack> loot = new ArrayList<>();

        if (!objective.canDrop()) return loot;

        LootUser user = plugin.getUserManager().getOrFetch(player);
        LootLimitData limitData = user.getLimitData();

        //Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player);
        double boost = plugin.getBoosterManager().getTotalBoost(player);

        for (Currency currency : EconomyBridge.getCurrencies()) {
            CurrencySettings settings = plugin.getCurrencyManager().getSettings(currency);
            if (settings == null) continue;

            // Do not drop money if limit is exceed and only if limit is still enabled.
            if (settings.hasDailyLimit() && limitData.isLimitExceed(currency, settings)) continue;

            for (int count = 0; count < instances; count++) {
                DropInfo dropInfo = objective.getCurrencyDrop(currency);
                double moneyAmount = currency.fineValue(dropInfo.rollAmountNaturally());

                // Apply boosters only for positive amount.
                if (moneyAmount > 0D) {
                    moneyAmount *= MoneyUtils.parseCustomMultiplier(player, dropInfo.getCustomMultiplier());
                    if (BoosterUtils.isApplicable(currency)) moneyAmount *= boost;
                }

                if (settings.isRoundToInt()) {
                    moneyAmount = Math.floor(moneyAmount);
                }

                // Do not drop zero amount.
                if (moneyAmount == 0D) continue;

                // Instant apply penalty and go to next currency.
                if (moneyAmount < 0D) {
                    this.pickupMoney(player, currency, moneyAmount, objective);
                    continue;
                }

                boolean dropped;

                // Add directly to balance if enabled.
                if (settings.isDirectToBalance()) {
                    dropped = this.pickupMoney(player, currency, moneyAmount, objective);
                }
                // Otherwise spawn money items depends on portions amount.
                else {
                    int portions = dropInfo.rollPortions();
                    double leftAmount = moneyAmount;

                    while (leftAmount > 0 && portions > 0) {
                        //System.out.println("moneyAmount = " + moneyAmount);
                        double cutAmount = portions == 1 ? leftAmount : settings.cutRandom(currency, leftAmount);// currency.round(Rnd.getDouble(leftAmount));
                        //System.out.println("cutAmount = " + cutAmount);
                        //System.out.println("==========================");
                        if (cutAmount == 0D) continue;

                        PlayerCurrencyLootCreateEvent event = new PlayerCurrencyLootCreateEvent(player, currency, cutAmount, objective);
                        plugin.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            ItemStack moneyItem = MoneyUtils.createMoney(event.getCurrency(), settings, event.getAmount(), player.getUniqueId(), event.getObjective());
                            loot.add(moneyItem);
                        }

                        leftAmount = currency.fineValue(leftAmount - cutAmount);
                        portions--;
                    }

                    dropped = !loot.isEmpty();
                }

                // Add limit only if currency was actually dropped/added.
                if (dropped && settings.hasDailyLimit()) {
                    if (!player.hasPermission(Perms.BYPASS_DAILY_LIMIT) && !player.hasPermission(Perms.PREFIX_BYPASS_DAILY_LIMIT + currency.getInternalId())) {
                        limitData.addCurrency(currency, moneyAmount);

                        if (limitData.isLimitExceed(currency, settings)) {
                            Lang.CURRENCY_LIMIT_NOTIFY.getMessage().send(player, replacer -> replacer
                                .replace(Placeholders.GENERIC_MAX, currency.format(settings.getDailyLimit()))
                                .replace(currency.replacePlaceholders())
                            );
                        }
                    }
                }
            }
        }

        return loot;
    }

    @NotNull
    public List<ItemStack> createDeathPenalty(@NotNull Player player) {
        List<ItemStack> list = new ArrayList<>();

        if (player.hasPermission(Perms.BYPASS_DEATH_PENALTY)) return list;
        if (!MoneyUtils.isMoneyAvailable(player)) return list;

        boolean isPvP;
        EntityDamageEvent damageEvent = player.getLastDamageCause();
        if (damageEvent != null) {
            Entity damager = MoneyUtils.getDamager(damageEvent);
            isPvP = damager instanceof Player && damager != player;
        }
        else isPvP = false;

        EconomyBridge.getCurrencies().forEach(currency -> {
            if (player.hasPermission(Perms.BYPASS_DEATH_PENALTY)) return;
            if (player.hasPermission(Perms.PREFIX_BYPASS_DEATH_PENALTY + currency.getInternalId())) return;

            CurrencySettings settings = plugin.getCurrencyManager().getSettings(currency);
            if (settings == null) return;

            DeathPenalty deathPenalty = settings.getDeathPenalty();
            if (!deathPenalty.isEnabled()) return;
            if (isPvP && !deathPenalty.isForPvP()) return;
            if (!isPvP && !deathPenalty.isForPvE()) return;

            if (!Rnd.chance(deathPenalty.getChance())) return;

            double percent = deathPenalty.rollAmount();
            if (percent <= 0) return;

            double balance = currency.getBalance(player);
            double amount = NumberUtil.round(balance * percent / 100D);
            if (amount <= 0D) return;

            if (this.loseMoney(player, currency, amount)) {
                if (deathPenalty.isDropItem()) {
                    ItemStack item = MoneyUtils.createMoney(currency, settings, amount);
                    list.add(item);
                }
            }
        });

        return list;
    }

    public void playMoneyEffect() {
        this.getTrackedLoot().forEach(item -> {
            Currency currency = this.getCurrency(item.getItemStack());
            if (currency == null) return;

            CurrencySettings settings = plugin.getCurrencyManager().getSettings(currency);
            if (settings == null) return;

            settings.getGroundEffect().play(item.getLocation().clone().add(0, 0.1, 0), 0.15, 0.15, 2);
        });
    }

    public void forcePickup() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getInventory().firstEmpty() != -1) continue;
            if (MoneyUtils.isDisabledWorld(player.getWorld())) continue;

            player.getNearbyEntities(2, 2, 2).stream().filter(e -> e instanceof Item).forEach(entity -> {
                Item item = (Item) entity;
                if (item.getPickupDelay() > 0) return;

                ItemStack stack = item.getItemStack();
                if (MoneyUtils.isMoney(stack)) {
                    EntityPickupItemEvent event = new EntityPickupItemEvent(player, item, 0);
                    plugin.getPluginManager().callEvent(event);
                }
            });
        }
    }

    public void mergeMoneyItems() {
        this.getTrackedLoot().stream().filter(Item::isOnGround).forEach(item -> {
            if (!item.isValid() || item.isDead()) return; // This is needed to due 'near' item removal.

            ItemStack originStack = item.getItemStack();
            Currency originCurrency = this.getCurrency(originStack);
            CurrencySettings settings = originCurrency == null ? null : plugin.getCurrencyManager().getSettings(originCurrency);
            if (originCurrency == null || settings == null) {
                item.remove();
                return;
            }

            UUID originOwner = MoneyUtils.getOwnerId(originStack);
            double originAmount = MoneyUtils.getMoneyAmount(originStack);

            for (Entity near : item.getNearbyEntities(5, 1, 5)) {
                if (!(near instanceof Item nearItem)) continue;

                ItemStack nearby = nearItem.getItemStack();
                if (!MoneyUtils.isMoney(nearby)) continue;

                UUID ownerName = MoneyUtils.getOwnerId(nearby);
                if (ownerName != null && !ownerName.equals(originOwner)) continue;

                Currency currency = this.getCurrency(nearby);
                if (currency != originCurrency) continue;

                originAmount += MoneyUtils.getMoneyAmount(nearby);
                near.remove();
            }

            ItemStack money = MoneyUtils.createMoney(originCurrency, settings, originAmount, originOwner);
            item.setCustomName(ItemUtil.getNameSerialized(money));
            item.setItemStack(money);
        });
    }

    /*@NotNull
    private Firework createFirework(@NotNull World world, @NotNull Location location) {
        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Type type = Rnd.get(FireworkEffect.Type.values());
        Color color = Color.fromBGR(Rnd.get(256), Rnd.get(256), Rnd.get(256));
        Color fade = Color.fromBGR(Rnd.get(256), Rnd.get(256), Rnd.get(256));
        FireworkEffect effect = FireworkEffect.builder()
            .flicker(Rnd.nextBoolean()).withColor(color).withFade(fade).with(type).trail(Rnd.nextBoolean()).build();

        meta.addEffect(effect);
        meta.setPower(Rnd.get(4));
        firework.setFireworkMeta(meta);
        PDCUtil.set(firework, Keys.SKILL_LEVEL_FIREWORK, true);
        return firework;
    }*/
}
