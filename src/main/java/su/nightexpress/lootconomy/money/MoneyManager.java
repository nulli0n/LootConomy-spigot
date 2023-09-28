package su.nightexpress.lootconomy.money;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.*;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.LootConomyAPI;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.api.event.PlayerCurrencyGainEvent;
import su.nightexpress.lootconomy.api.event.PlayerCurrencyLoseEvent;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Keys;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.money.listener.MoneyExploitListener;
import su.nightexpress.lootconomy.money.listener.MoneyGenericListener;
import su.nightexpress.lootconomy.money.task.InventoryCheckTask;
import su.nightexpress.lootconomy.money.task.MoneyEffectTask;
import su.nightexpress.lootconomy.money.task.MoneyMergeTask;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.skill.impl.SkillObjective;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MoneyManager extends AbstractManager<LootConomy> {

    private final Set<Item> trackedLoot;

    private InventoryCheckTask inventoryCheckTask;
    private MoneyMergeTask     moneyMergeTask;
    private MoneyEffectTask moneyEffectTask;

    public MoneyManager(@NotNull LootConomy plugin) {
        super(plugin);
        this.trackedLoot = ConcurrentHashMap.newKeySet();
    }

    @Override
    protected void onLoad() {
        this.addListener(new MoneyGenericListener(this));
        this.addListener(new MoneyExploitListener(this.plugin));

        this.inventoryCheckTask = new InventoryCheckTask(this);
        this.inventoryCheckTask.start();

        if (Config.GENERAL_LOOT_MERGING.get()) {
            this.moneyMergeTask = new MoneyMergeTask(this);
            this.moneyMergeTask.start();
        }

        this.moneyEffectTask = new MoneyEffectTask(this);
        this.moneyEffectTask.start();
    }

    @Override
    protected void onShutdown() {
        this.trackedLoot.clear();
        if (this.moneyEffectTask != null) this.moneyEffectTask.stop();
        if (this.inventoryCheckTask != null) {
            this.inventoryCheckTask.stop();
            this.inventoryCheckTask = null;
        }
        if (this.moneyMergeTask != null) {
            this.moneyMergeTask.stop();
            this.moneyMergeTask = null;
        }
    }

    @NotNull
    public Set<Item> getTrackedLoot() {
        this.trackedLoot.removeIf(item -> !item.isValid() || item.isDead());
        return trackedLoot;
    }

    public static boolean isMoneyAvailable(@NotNull Player player) {
        if (EntityUtil.isNPC(player)) return false;
        if (Config.GENERAL_DISABLED_WORLDS.get().contains(player.getWorld().getName())) return false;
        if (Config.EXPLOIT_IGNORE_GAME_MODES.get().contains(player.getGameMode())) return false;

        return true;
    }

    public static boolean isMoneyItem(@NotNull ItemStack item) {
        return PDCUtil.getDouble(item, Keys.ITEM_AMOUNT).isPresent();
    }

    public static boolean isMoneyOwner(@NotNull ItemStack item, @NotNull Player player) {
        String owner = getMoneyOwner(item);
        return owner == null || player.getName().equalsIgnoreCase(owner);
    }

    @Nullable
    public static String getMoneyOwner(@NotNull ItemStack item) {
        return PDCUtil.getString(item, Keys.ITEM_OWNER).orElse(null);
    }

    @Nullable
    public static Skill getMoneyJob(@NotNull ItemStack item) {
        String skillId = PDCUtil.getString(item, Keys.ITEM_SKILL).orElse(null);
        return skillId == null ? null : LootConomyAPI.getSkillById(skillId);
    }

    @Nullable
    public static SkillObjective getMoneyObjective(@NotNull ItemStack item) {
        String type = PDCUtil.getString(item, Keys.ITEM_OBJECTIVE).orElse(null);
        if (type == null) return null;

        Skill skill = getMoneyJob(item);
        return skill == null ? null : skill.getObjective(type);
    }

    @Nullable
    public static Currency getMoneyCurrency(@NotNull ItemStack item) {
        String currencyId = PDCUtil.getString(item, Keys.ITEM_CURRENCY).orElse(null);
        return currencyId == null ? null : LootConomyAPI.getCurrency(currencyId);
    }

    public static double getMoneyAmount(@NotNull ItemStack item) {
        return PDCUtil.getDouble(item, Keys.ITEM_AMOUNT).orElse(0D);
    }

    public static void devastateEntity(@NotNull Entity entity) {
        PDCUtil.set(entity, Keys.LOOT_EMPTY, true);
    }

    public static boolean isDevastated(@NotNull Entity entity) {
        return PDCUtil.getBoolean(entity, Keys.LOOT_EMPTY).isPresent();
    }

    public boolean pickupMoney(@NotNull Player player, @NotNull ItemStack item) {
        Currency currency = getMoneyCurrency(item);
        if (currency == null) return false;

        double money = getMoneyAmount(item);
        Skill skill = getMoneyJob(item);
        SkillObjective objective = getMoneyObjective(item);

        return this.pickupMoney(player, currency, money, skill, objective);
    }

    public boolean pickupMoney(@NotNull Player player, @NotNull Currency currency, double money,
                               @Nullable Skill skill, @Nullable SkillObjective objective) {
        boolean isLose = money < 0D;

        money = currency.round(money);
        if (isLose) {
            return this.loseMoney(player, currency, money, skill, objective);
        }
        else {
            return this.gainMoney(player, currency, money, skill, objective);
        }
    }

    public boolean loseMoney(@NotNull Player player, @NotNull Currency currency, double money) {
        return this.loseMoney(player, currency, money, null, null);
    }

    public boolean loseMoney(@NotNull Player player, @NotNull Currency currency, double money,
                             @Nullable Skill skill, @Nullable SkillObjective objective) {
        PlayerCurrencyLoseEvent event = new PlayerCurrencyLoseEvent(player, currency, money, skill, objective);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        money = event.getAmount();
        currency = event.getCurrency();
        currency.getHandler().take(player, money);

        plugin.getMessage(Lang.CURRENCY_LOST)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(money))
            .replace(Placeholders.GENERIC_BALANCE, currency.format(currency.getHandler().getBalance(player)))
            .send(player);
        return true;
    }

    public boolean gainMoney(@NotNull Player player, @NotNull Currency currency, double money) {
        return this.gainMoney(player, currency, money, null, null);
    }

    public boolean gainMoney(@NotNull Player player, @NotNull Currency currency, double money,
                             @Nullable Skill skill, @Nullable SkillObjective objective) {
        PlayerCurrencyGainEvent event = new PlayerCurrencyGainEvent(player, currency, money, skill, objective);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        money = event.getAmount();
        currency = event.getCurrency();
        currency.getHandler().give(player, money);

        LootUser user = plugin.getUserManager().getUserData(player);
        if (user.getSettings().isPickupSound()) {
            currency.getPickupSound().play(player);
        }

        plugin.getMessage(Lang.CURRENCY_PICKUP)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(money))
            .replace(Placeholders.GENERIC_BALANCE, currency.format(currency.getHandler().getBalance(player)))
            .send(player);
        return true;
    }

    @NotNull
    public static ItemStack createMoney(@NotNull Currency currency, double amount,
                                 @Nullable Player owner,
                                 @Nullable Skill skill,
                                 @Nullable SkillObjective objective) {
        if (amount == 0D) throw new IllegalArgumentException("Money amount can not be zero!");

        // Get item model depends on the money amount.
        ItemStack item = currency.getIcon(amount);

        // Set money and job (if present) tags.
        double finalMoney = currency.round(amount);
        ItemUtil.mapMeta(item, meta -> {
            PDCUtil.set(meta, Keys.ITEM_AMOUNT, finalMoney);
            PDCUtil.set(meta, Keys.ITEM_CURRENCY, currency.getId());
            PDCUtil.set(meta, Keys.ITEM_ID, UUID.randomUUID().toString());
            if (skill != null) PDCUtil.set(meta, Keys.ITEM_SKILL, skill.getId());
            if (objective != null) PDCUtil.set(meta, Keys.ITEM_OBJECTIVE, objective.getName());

            // Add owner protection for money item.
            if (Config.GENERAL_LOOT_PROTECTION.get() && owner != null) {
                PDCUtil.set(meta, Keys.ITEM_OWNER, owner.getName());
            }

            // And now replace visuals.
            if (meta.hasDisplayName()) {
                String name = Colorizer.apply(meta.getDisplayName().replace(Placeholders.GENERIC_AMOUNT, currency.format(finalMoney)));
                meta.setDisplayName(name);
            }
        });
        return item;
    }
}
