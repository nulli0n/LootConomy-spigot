package su.nightexpress.lootconomy.money;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.action.ActionType;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.api.event.PlayerCurrencyGainEvent;
import su.nightexpress.lootconomy.api.event.PlayerCurrencyLootCreateEvent;
import su.nightexpress.lootconomy.api.event.PlayerCurrencyLoseEvent;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.data.impl.LootLimitData;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.money.listener.AbuseListener;
import su.nightexpress.lootconomy.money.listener.MoneyItemListener;
import su.nightexpress.lootconomy.money.menu.ObjectiveTypesMenu;
import su.nightexpress.lootconomy.money.menu.ObjectivesMenu;
import su.nightexpress.lootconomy.money.object.DeathPenalty;
import su.nightexpress.lootconomy.money.object.DropInfo;
import su.nightexpress.lootconomy.money.object.MoneyObjective;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.message.LangMessage;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MoneyManager extends AbstractManager<LootConomyPlugin> {

    private final Map<ActionType<?, ?>, Map<String, MoneyObjective>> objectiveMap;

    private final Set<Item> trackedLoot;

    private ObjectiveTypesMenu objectiveTypesMenu;
    private ObjectivesMenu     objectivesMenu;

    public MoneyManager(@NotNull LootConomyPlugin plugin) {
        super(plugin);
        this.objectiveMap = new HashMap<>();
        this.trackedLoot = ConcurrentHashMap.newKeySet();
    }

    @Override
    protected void onLoad() {
        this.createDefaults();
        this.loadObjectives();

        this.objectiveTypesMenu = new ObjectiveTypesMenu(this.plugin);
        this.objectivesMenu = new ObjectivesMenu(this.plugin);

        this.addListener(new MoneyItemListener(this.plugin, this));
        this.addListener(new AbuseListener(this.plugin));

        if (Config.LOOT_MERGE_ENABLED.get()) {
            this.addTask(this.plugin.createTask(this::mergeMoneyItems).setSecondsInterval(Config.LOOT_MERGE_INTERVAL.get()));
        }
        if (Config.INVENTORY_SIZE_BYPASS_ENABLED.get()) {
            this.addTask(this.plugin.createTask(this::forcePickup).setTicksInterval(Config.INVENTORY_SIZE_BYPASS_INTERVAL.get()));
        }
        this.addTask(this.plugin.createAsyncTask(this::playMoneyEffect).setTicksInterval(5L));
    }

    @Override
    protected void onShutdown() {
        if (this.objectivesMenu != null) this.objectivesMenu.clear();
        if (this.objectiveTypesMenu != null) this.objectiveTypesMenu.clear();

        this.trackedLoot.clear();
        this.objectiveMap.clear();
    }

    private void createDefaults() {
        Creator creator = new Creator(this.plugin);
        creator.create();
    }

    private void loadObjectives() {
        for (FileConfig config : FileConfig.loadAll(this.plugin.getDataFolder() + Config.DIR_OBJECTIVES)) {
            for (String object : config.getSection("")) {
                MoneyObjective objective = MoneyObjective.read(plugin, config, object, object);
                if (objective == null) continue;

                this.objectiveMap.computeIfAbsent(objective.getActionType(), k -> new HashMap<>()).put(objective.getId(), objective);
            }
            config.saveChanges();
        }
    }

    public void openObjectivesMenu(@NotNull Player player) {
        this.objectiveTypesMenu.open(player);
    }

    public void openObjectivesMenu(@NotNull Player player, @NotNull ActionType<?, ?> type) {
        this.objectivesMenu.open(player, type);
    }

    @NotNull
    public Map<ActionType<?, ?>, Map<String, MoneyObjective>> getObjectiveMap() {
        return objectiveMap;
    }

    @NotNull
    public Map<String, MoneyObjective> getObjectivesMap(@NotNull ActionType<?, ?> actionType) {
        return this.objectiveMap.getOrDefault(actionType, Collections.emptyMap());
    }

    @NotNull
    public Collection<MoneyObjective> getObjectives(@NotNull ActionType<?, ?> actionType) {
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

    @Nullable
    public MoneyObjective getObjective(@NotNull ActionType<?, ?> actionType, @NotNull String id) {
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

        return this.plugin.getCurrencyManager().getCurrency(currencyId);
    }

    @Nullable
    public MoneyObjective getObjective(@NotNull ItemStack itemStack) {
        String objectiveId = MoneyUtils.getObjectiveId(itemStack);
        if (objectiveId == null) return null;

        String actionName = MoneyUtils.getActionName(itemStack);
        if (actionName == null) return null;

        ActionType<?, ?> actionType = this.plugin.getActionRegistry().getActionType(actionName);
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

    public boolean loseMoney(@NotNull Player player, @NotNull Currency currency, double money, @Nullable MoneyObjective objective) {
        PlayerCurrencyLoseEvent event = new PlayerCurrencyLoseEvent(player, currency, money, objective);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        money = event.getAmount();
        currency = event.getCurrency();
        currency.getHandler().take(player, money);

        LangMessage message = (objective == null ? Lang.CURRENCY_LOST_DEATH : Lang.CURRENCY_LOST_PENALTY).getMessage()
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(money))
            .replace(Placeholders.GENERIC_BALANCE, currency.format(currency.getHandler().getBalance(player)));

        if (objective != null) {
            message = message.replace(Placeholders.GENERIC_NAME, objective.getDisplayName());
        }
        message.send(player);

        return true;
    }

    public boolean gainMoney(@NotNull Player player, @NotNull Currency currency, double money) {
        return this.gainMoney(player, currency, money, null);
    }

    public boolean gainMoney(@NotNull Player player, @NotNull Currency currency, double money, @Nullable MoneyObjective objective) {
        PlayerCurrencyGainEvent event = new PlayerCurrencyGainEvent(player, currency, money, objective);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        money = event.getAmount();
        currency = event.getCurrency();
        currency.getHandler().give(player, money);

        LootUser user = plugin.getUserManager().getUserData(player);
        if (user.getSettings().isPickupSound()) {
            currency.getPickupSound().play(player);
        }

        Lang.CURRENCY_PICKUP.getMessage()
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(money))
            .replace(Placeholders.GENERIC_BALANCE, currency.format(currency.getHandler().getBalance(player)))
            .send(player);

        return true;
    }

    @NotNull
    public <O> List<ItemStack> createLoot(@NotNull Player player, @NotNull ActionType<?, O> type, @NotNull O object) {
        return this.createLoot(player, type, object, 1);
    }

    @NotNull
    public <O> List<ItemStack> createLoot(@NotNull Player player, @NotNull ActionType<?, O> type, @NotNull O object, int amount) {
        List<ItemStack> loot = new ArrayList<>();
        this.getObjectives(type).forEach(objective -> {
            if (objective.hasObject(type.getObjectName(object))) {
                loot.addAll(this.createLoot(player, objective, amount));
            }
        });
        return loot;
    }

    @NotNull
    public List<ItemStack> createLoot(@NotNull Player player, @NotNull MoneyObjective objective) {
        return this.createLoot(player, objective, 1);
    }

    @NotNull
    public List<ItemStack> createLoot(@NotNull Player player, @NotNull MoneyObjective objective, int amount) {
        List<ItemStack> loot = new ArrayList<>();

        if (!objective.canDrop()) return loot;

        LootUser user = plugin.getUserManager().getUserData(player);
        LootLimitData limitData = user.getLimitData();

        Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player);

        for (Currency currency : plugin.getCurrencyManager().getCurrencies()) {
            if (currency.hasDailyLimit() && limitData.isLimitExceed(currency)) continue; // Do not attempt to drop money if limit is exceed and only if limit is still enabled.

            for (int count = 0; count < amount; count++) {
                DropInfo dropInfo = objective.getCurrencyDrop(currency);
                double moneyAmount = currency.round(dropInfo.rollAmountNaturally());

                // Apply boosters only for positive amount.
                if (moneyAmount > 0D) {
                    moneyAmount *= MoneyUtils.parseCustomMultiplier(player, dropInfo.getCustomMultiplier());
                    moneyAmount *= Booster.getMultiplier(currency, boosters);
                }

                if (currency.isRoundToInt()) {
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
                if (currency.isDirectToBalance()) {
                    dropped = this.pickupMoney(player, currency, moneyAmount, objective);
                }
                // Otherwise spawn money items depends on portions amount.
                else {
                    int portions = dropInfo.rollPortions();
                    double leftAmount = moneyAmount;

                    while (leftAmount > 0 && portions > 0) {
                        //System.out.println("moneyAmount = " + moneyAmount);
                        double cutAmount = portions == 1 ? leftAmount : currency.cutRandom(leftAmount);// currency.round(Rnd.getDouble(leftAmount));
                        //System.out.println("cutAmount = " + cutAmount);
                        //System.out.println("==========================");
                        if (cutAmount == 0D) continue;

                        PlayerCurrencyLootCreateEvent event = new PlayerCurrencyLootCreateEvent(player, currency, cutAmount, objective);
                        plugin.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            ItemStack moneyItem = MoneyUtils.createMoney(event.getCurrency(), event.getAmount(), player.getUniqueId(), event.getObjective());
                            loot.add(moneyItem);
                        }

                        leftAmount = currency.round(leftAmount - cutAmount);
                        portions--;
                    }

                    dropped = !loot.isEmpty();
                }

                // Add limit only if currency was actually dropped/added.
                if (dropped && currency.hasDailyLimit()) {
                    if (!player.hasPermission(Perms.BYPASS_DAILY_LIMIT) && !player.hasPermission(Perms.PREFIX_BYPASS_DAILY_LIMIT + currency.getId())) {
                        limitData.addCurrency(currency, moneyAmount);

                        if (limitData.isLimitExceed(currency)) {
                            Lang.CURRENCY_LIMIT_NOTIFY.getMessage()
                                .replace(Placeholders.GENERIC_MAX, currency.format(currency.getDailyLimit()))
                                .replace(currency.replacePlaceholders())
                                .send(player);
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

        this.plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
            if (player.hasPermission(Perms.BYPASS_DEATH_PENALTY)) return;
            if (player.hasPermission(Perms.PREFIX_BYPASS_DEATH_PENALTY + currency.getId())) return;

            DeathPenalty deathPenalty = currency.getDeathPenalty();
            if (!deathPenalty.isEnabled()) return;
            if (isPvP && !deathPenalty.isForPvP()) return;
            if (!isPvP && !deathPenalty.isForPvE()) return;

            if (!Rnd.chance(deathPenalty.getChance())) return;

            double percent = deathPenalty.rollAmount();
            if (percent <= 0) return;

            double balance = currency.getHandler().getBalance(player);
            double amount = NumberUtil.round(balance * percent / 100D);
            if (amount <= 0D) return;

            if (this.loseMoney(player, currency, amount)) {
                if (deathPenalty.isDropItem()) {
                    ItemStack item = MoneyUtils.createMoney(currency, amount);
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

            currency.getGroundEffect().play(item.getLocation().clone().add(0, 0.1, 0), 0.15, 0.15, 2);
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
            if (originCurrency == null) {
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

            ItemStack money = MoneyUtils.createMoney(originCurrency, originAmount, originOwner);
            item.setCustomName(ItemUtil.getItemName(money));
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
