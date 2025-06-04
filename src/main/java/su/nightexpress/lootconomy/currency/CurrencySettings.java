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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    // Single Random instance for deterministic splitting
    private static final Random SPLIT_RANDOM = new Random();

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
                "Sets whether dropped item amount will be rounded to a whole number instead of being decimal."
        ).read(config);

        boolean directToBalance = ConfigValue.create(path + ".Instant_Pickup",
                false,
                "Sets whether currency will be instantly added to a player's balance when dropped."
        ).read(config);

        double dailyLimit = ConfigValue.create(path + ".Daily_Limit",
                -1D,
                "How much currency can be earned daily per player. Reset at midnight.",
                "Set to '0' or '-1' to disable."
        ).read(config);

        UniParticle groundEffect = ConfigValue.create(path + ".Effects.Particle_On_Ground",
                UniParticle.redstone(Color.YELLOW, 1f),
                "Particle effect for the dropped currency item."
        ).read(config);

        if (config.contains(path + ".Effects.Pickup_Sound.Name")) {
            NightSound.read(config, path + ".Effects.Pickup_Sound");
        }

        NightSound pickupSound = ConfigValue.create(path + ".Effects.Pickup_Sound",
                NightSound.of(Sound.BLOCK_NOTE_BLOCK_BELL),
                "Sound effect when a player picks up a currency item."
        ).read(config);

        DeathPenalty deathPenalty = DeathPenalty.read(config, path + ".Death_Penalty");

        TreeMap<Integer, ItemStack> itemStyle = ConfigValue.forTreeMap(path + ".Item_Style_By_Amount",
                (string) -> NumberUtil.getInteger(string, 0),
                (cfg, path2, key) -> cfg.getItem(path2 + "." + key),
                (cfg, path2, map) -> map.forEach((amount, item) -> cfg.setItem(path2 + "." + amount, item)),
                () -> {
                    TreeMap<Integer, ItemStack> map = new TreeMap<>();
                    if (currency.getInternalId().equalsIgnoreCase(CurrencyId.VAULT)) {
                        map.put(0,    getDefaultItem(Material.GOLD_NUGGET));
                        map.put(100,  getDefaultItem(Material.GOLD_INGOT));
                        map.put(1000, getDefaultItem(Material.GOLD_BLOCK));
                    } else {
                        map.put(0, currency.getDefaultIcon());
                    }
                    return map;
                },
                "Create different item styles depending on the dropped amount.",
                "Uses the item for the greatest key ≤ the currency amount.",
                Placeholders.URL_WIKI_ITEMS,
                "[*] Useless for some currencies (e.g. " + HookId.COINS_ENGINE + ")"
        ).read(config);

        itemStyle.values().removeIf(item -> item.getType().isAir());
        if (itemStyle.isEmpty()) {
            itemStyle.put(0, currency.getDefaultIcon());
        }

        return new CurrencySettings(
                dropFormat,
                groundEffect,
                pickupSound,
                roundToInt,
                directToBalance,
                dailyLimit,
                deathPenalty,
                itemStyle
        );
    }

    @NotNull
    private static ItemStack getDefaultItem(@NotNull Material material) {
        ItemStack item = new ItemStack(material);
        ItemUtil.editMeta(item, meta -> meta.addEnchant(Enchantment.UNBREAKING, 1, true));
        return item;
    }

    @NotNull
    public String dropFormat(@NotNull Currency currency, double amount) {
        return currency.applyFormat(this.dropFormat, amount);
    }

    /**
     * Splits `fullAmount` (in the currency's smallest unit) into `parts` random portions.
     * Always terminates in O(parts·log parts) time.
     *
     * @param currency   The Currency instance, used to apply fineValue() on each piece.
     * @param fullAmount The total amount (e.g., in “cents”) to split.
     * @param parts      Number of random pieces to generate (must be ≥ 1).
     * @return A List of length `parts`, each a double ≥ 0, summing (after fineValue) ≈ fullAmount.
     *         If fullAmount < parts, some pieces may be zero; each is wrapped through currency.fineValue().
     */
    @NotNull
    public List<Double> cutRandom(@NotNull Currency currency, double fullAmount, int parts) {
        List<Double> result = new ArrayList<>();
        if (parts <= 0) {
            return result;
        }

        // If fullAmount is smaller than parts, assign 1 unit to integer(floor(fullAmount)) pieces, rest zero.
        if (fullAmount < parts) {
            int integerPortion = (int) Math.floor(fullAmount);
            for (int i = 0; i < integerPortion; i++) {
                result.add(currency.fineValue(1.0));
            }
            for (int i = integerPortion; i < parts; i++) {
                result.add(0.0);
            }
            return result;
        }

        // Generate (parts - 1) random cut points in [0, fullAmount]
        List<Double> cuts = new ArrayList<>(parts + 1);
        cuts.add(0.0);
        for (int i = 0; i < parts - 1; i++) {
            cuts.add(SPLIT_RANDOM.nextDouble() * fullAmount);
        }
        cuts.add(fullAmount);

        Collections.sort(cuts);

        // Differences between consecutive cuts form each piece
        for (int i = 1; i < cuts.size(); i++) {
            double portion = cuts.get(i) - cuts.get(i - 1);
            if (this.isRoundToInt()) {
                portion = Math.ceil(portion);
            }
            result.add(currency.fineValue(portion));
        }
        return result;
    }

    /**
     * Deprecated: Use cutRandom(Currency, double, int) instead.
     * Picks one random “cut” in [0, fullAmount], applies rounding if enabled, then fineValue().
     */
    @Deprecated
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

    /**
     * Returns the appropriate ItemStack icon for a given amount, based on configured thresholds.
     */
    @NotNull
    public ItemStack getIcon(double amount) {
        Map.Entry<Integer, ItemStack> entry = this.itemStyle.floorEntry((int) Math.abs(amount));
        if (entry == null) {
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(entry.getValue());
    }
}
