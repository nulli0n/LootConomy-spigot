package su.nightexpress.lootconomy.currency;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.lootconomy.money.object.DeathPenalty;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightSound;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.util.Map;
import java.util.TreeMap;

public class CurrencySettings {

    private final String      dropFormat;
    private final UniParticle groundEffect;
    private final NightSound  pickupSound;
    private final boolean     roundToInt;
    private final boolean     directToBalance;
    private final double      dailyLimit;
    private final DeathPenalty deathPenalty;

    private final TreeMap<Integer, ItemStack> itemStyle;

    public CurrencySettings(
        @NotNull String dropFormat,
        @NotNull UniParticle groundEffect,
        @NotNull NightSound pickupSound,
        boolean roundToInt,
        boolean directToBalance,
        double dailyLimit,
        @NotNull DeathPenalty deathPenalty,
        @NotNull TreeMap<Integer, ItemStack> itemStyle
    ) {
        this.dropFormat = dropFormat;
        this.groundEffect = groundEffect;
        this.pickupSound = pickupSound;
        this.roundToInt = roundToInt;
        this.directToBalance = directToBalance;
        this.dailyLimit = dailyLimit;
        this.deathPenalty = deathPenalty;

        this.itemStyle = itemStyle;
    }

    @NotNull
    public static CurrencySettings read(@NotNull FileConfig config, @NotNull String path, @NotNull Currency currency) {
        String dropFormat = ConfigValue.create(path + ".Format.Dropped",
            currency.getFormat(),
            "Currency format to display on dropped item. Available placeholders:",
            "- " + Placeholders.GENERIC_AMOUNT,
            "- " + Placeholders.GENERIC_NAME
        ).read(config);

        boolean roundToInt = ConfigValue.create(path + ".Round_To_Int",
            false,
            "Sets whether or not dropped item amount will be rounded to a whole number (integer) instead of being decimal."
        ).read(config);

        boolean directToBalance = ConfigValue.create(path + ".Instant_Pickup",
            false,
            "Sets whether or not this currnecy will be directly added to player's balance when dropped."
        ).read(config);

        double dailyLimit = ConfigValue.create(path + ".Daily_Limit",
            -1D,
            "Sets how much currency can be earned daily on per-player basis.",
            "Limit resets at new day's midnight.",
            "Set to '0' or '-1' to disable."
        ).read(config);

        UniParticle groundEffect = ConfigValue.create(path + ".Effects.Particle_On_Ground",
            UniParticle.redstone(Color.YELLOW, 1f),
            "Sets particle effect for the dropped currency item."
        ).read(config);

        // Update
        if (config.contains(path + ".Effects.Pickup_Sound.Name")) {
            NightSound.read(config, path + ".Effects.Pickup_Sound");
        }

        NightSound pickupSound = ConfigValue.create(path + ".Effects.Pickup_Sound",
            NightSound.of(Sound.BLOCK_NOTE_BLOCK_BELL),
            "Sets sound effect for a player who pickups currency item."
        ).read(config);

        DeathPenalty deathPenalty = DeathPenalty.read(config, path + ".Death_Penalty");

        TreeMap<Integer, ItemStack> itemStyle = ConfigValue.forTreeMap(path + ".Item_Style_By_Amount",
            (string) -> NumberUtil.getInteger(string, 0),
            (cfg, path2, key) -> cfg.getItem(path2 + "." + key),
            (cfg, path2, map) -> map.forEach((amount, item) -> cfg.setItem(path2 + "." + amount, item)),
            () -> {
                TreeMap<Integer, ItemStack> map = new TreeMap<>();
                if (currency.getInternalId().equalsIgnoreCase(CurrencyId.VAULT)) {
                    map.put(0, getDefaultItem(Material.GOLD_NUGGET));
                    map.put(100, getDefaultItem(Material.GOLD_INGOT));
                    map.put(1000, getDefaultItem(Material.GOLD_BLOCK));
                }
                else {
                    map.put(0, currency.getDefaultIcon());
                }
                return map;
            },
            "Here you can create different item styles depends on the dropped amount.",
            "It will use item of the greatest amount less than or equal to the currency amount.",
            Placeholders.URL_WIKI_ITEMS,
            "[*] This setting is useless for some currencies (e.g. " + HookId.COINS_ENGINE + ")"
        ).read(config);

        itemStyle.values().removeIf(item -> item.getType().isAir());

        if (itemStyle.isEmpty()) {
            itemStyle.put(0, currency.getDefaultIcon());
        }

        return new CurrencySettings(dropFormat, groundEffect, pickupSound, roundToInt, directToBalance, dailyLimit, deathPenalty, itemStyle);
    }

    @NotNull
    private static ItemStack getDefaultItem(@NotNull Material material) {
        ItemStack item = new ItemStack(material);
        ItemUtil.editMeta(item, meta -> {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        });
        return item;
    }

    @NotNull
    public String dropFormat(@NotNull Currency currency, double amount) {
        return currency.applyFormat(this.dropFormat, amount);
    }

    public double cutRandom(@NotNull Currency currency, double fullAmount) {
        double cut = Rnd.getDouble(fullAmount);

        if (this.isRoundToInt()) {
            cut = Math.ceil(cut);
        }

        return currency.fineValue(cut);
    }

    public boolean hasDailyLimit() {
        return this.getDailyLimit() >= 0D;
    }

    @NotNull
    public String getDropFormat() {
        return dropFormat;
    }

    public boolean isRoundToInt() {
        return roundToInt;
    }

    public boolean isDirectToBalance() {
        return directToBalance;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    @NotNull
    public UniParticle getGroundEffect() {
        return groundEffect;
    }

    @NotNull
    public NightSound getPickupSound() {
        return pickupSound;
    }

    @NotNull
    public DeathPenalty getDeathPenalty() {
        return deathPenalty;
    }

    @NotNull
    public ItemStack getIcon(double amount) {
        Map.Entry<Integer, ItemStack> entry = this.itemStyle.floorEntry((int) Math.abs(amount));
        if (entry == null) {
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(entry.getValue());
    }
}
