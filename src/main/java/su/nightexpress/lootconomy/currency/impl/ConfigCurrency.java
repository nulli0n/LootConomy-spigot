package su.nightexpress.lootconomy.currency.impl;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.lang.LangColors;
import su.nexmedia.engine.api.manager.AbstractConfigHolder;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.api.currency.CurrencyHandler;
import su.nightexpress.lootconomy.hook.HookId;

import java.util.Map;
import java.util.TreeMap;

public class ConfigCurrency extends AbstractConfigHolder<LootConomy> implements Currency {

    private   String   name;
    private   String   format;

    private SimpleParticle groundEffect;
    private Sound          pickupSound;

    private boolean directToBalance;
    private boolean  deathPenaltyEnabled;
    private boolean  deathPenaltyDropItem;
    private double deathPenaltyChance;
    private Pair<Double, Double> deathPenaltyAmount;

    private TreeMap<Integer, ItemStack> itemStyle;

    private final CurrencyHandler handler;
    private final PlaceholderMap placeholderMap;

    public ConfigCurrency(@NotNull LootConomy plugin, @NotNull JYML cfg, @NotNull CurrencyHandler handler) {
        super(plugin, cfg);
        this.handler = handler;

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.CURRENCY_ID, this::getId)
            .add(Placeholders.CURRENCY_NAME, this::getName)
        ;
    }

    @Override
    public boolean load() {
        boolean enabled = JOption.create("Enabled", true,
            "Sets whether or not this currency is enabled.").read(cfg);

        this.setName(JOption.create("Name", StringUtil.capitalizeUnderscored(this.getId()),
            "Sets currency display name.").read(cfg));

        this.format = JOption.create("Format", Placeholders.GENERIC_AMOUNT + " " + Placeholders.CURRENCY_NAME,
            "Sets currency format.", "Available placeholders:",
            "- " + su.nightexpress.lootconomy.Placeholders.GENERIC_AMOUNT, "- " + Placeholders.CURRENCY_NAME,
            "This option is useless for " + HookId.COINS_ENGINE + " (it has own format setting).").mapReader(Colorizer::apply).read(cfg);

        this.directToBalance = JOption.create("Instant_Pickup", false,
            "Sets whether or not this currnecy will be directly added to player's balance when dropped.").read(cfg);

        this.groundEffect = new JOption<>("Effects.Particle_On_Ground",
            (cfg, path, def) -> SimpleParticle.read(cfg, path),
            SimpleParticle.of(Particle.REDSTONE, new Particle.DustOptions(Color.YELLOW, 1f)),
            "Sets the particle effect to be constantly played when currency item is dropped in the world.",
            Placeholders.URL_ENGINE_PARTICLE
        ).setWriter((cfg, path, particle) -> particle.write(cfg, path)).read(cfg);

        this.pickupSound = JOption.create("Effects.Pickup_Sound", Sound.class, Sound.BLOCK_NOTE_BLOCK_BELL,
            "Sets sound to player when player pickups item of that currency.",
            Placeholders.URL_SPIGOT_SOUND).read(cfg);



        String path = "Death_Penalty.";
        this.deathPenaltyEnabled = JOption.create(path + "Enabled", false,
            "Sets whether or not death penalty is enabled for this currency.",
            "When enabled, players will lose specified % of their balance on death.").read(cfg);

        this.deathPenaltyDropItem = !JOption.create(path + "Do_Not_Drop_Item", false,
            "Sets whether or not a currency item should NOT be dropped on player's death.",
            "If disabled, this means that player can return to death location and pickup it back.").read(cfg);

        this.deathPenaltyChance = JOption.create(path + "Chance", 25D,
            "Sets the chance that death penalty will apply on death.").read(cfg);

        double penMin = JOption.create(path + "Percent_Of_Balance.Minimal", 1.0,
            "Sets minimal value (in % of player's balance) to be dropped.").read(cfg);

        double penMax = JOption.create(path + "Percent_Of_Balance.Maximal", 3.0,
            "Sets maximal value (in % of player's balance) to be dropped.").read(cfg);

        this.deathPenaltyAmount = Pair.of(penMin, penMax);

        this.itemStyle = new TreeMap<>(JOption.forMap("Item_Style_By_Amount",
            (raw) -> StringUtil.getInteger(raw, 0),
            (cfg, path2, key) -> cfg.getItem(path2 + "." + key),
            Map.of(
                0, getDefaultItem(Material.GOLD_NUGGET),
                100, getDefaultItem(Material.GOLD_INGOT),
                1000, getDefaultItem(Material.GOLD_BLOCK)
            ),
            "Here you can define different item layout depends on the currency amount.",
            "It will use item of the greatest amount less than or equal to the currency amount.",
            Placeholders.URL_ENGINE_ITEMS,
            "Use '" + Placeholders.GENERIC_AMOUNT + "' placeholer to display formatted currency amount in item name."
        ).setWriter((cfg, path2, map) -> map.forEach((i, item) -> cfg.setItem(path2 + "." + i, item))).read(cfg));
        this.itemStyle.values().removeIf(item -> item.getType().isAir());

        this.cfg.saveChanges();

        if (this.itemStyle.isEmpty()) {
            this.plugin.warn("No valid items are defined. Check currency item style settings.");
            return false;
        }
        return true;
    }

    @NotNull
    private static ItemStack getDefaultItem(@NotNull Material material) {
        ItemStack item = new ItemStack(material);
        ItemUtil.mapMeta(item, meta -> {
            meta.setDisplayName(LangColors.YELLOW + Placeholders.GENERIC_AMOUNT);
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        });
        return item;
    }

    @Override
    public void onSave() {
        this.cfg.set("Name", this.getName());
        this.cfg.set("Format", this.getFormat());
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    @Override
    public CurrencyHandler getHandler() {
        return handler;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = Colorizer.apply(name);
    }

    @NotNull
    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public boolean isDirectToBalance() {
        return directToBalance;
    }

    @Nullable
    @Override
    public SimpleParticle getGroundEffect() {
        return groundEffect;
    }

    @Nullable
    @Override
    public Sound getPickupSound() {
        return pickupSound;
    }

    @Override
    public boolean isDeathPenaltyEnabled() {
        return deathPenaltyEnabled;
    }

    @Override
    public boolean isDeathPenaltyDropItem() {
        return deathPenaltyDropItem;
    }

    @Override
    public double getDeathPenaltyChance() {
        return deathPenaltyChance;
    }

    @Override
    public double getDeathPenaltyAmountMin() {
        return this.deathPenaltyAmount.getFirst();
    }

    @Override
    public double getDeathPenaltyAmountMax() {
        return this.deathPenaltyAmount.getSecond();
    }

    public double getDeathPenaltyAmount() {
        return Rnd.getDouble(this.getDeathPenaltyAmountMin(), this.getDeathPenaltyAmountMax());
    }

    @Override
    @NotNull
    public ItemStack getIcon(double amount) {
        Map.Entry<Integer, ItemStack> entry = this.itemStyle.floorEntry((int) Math.abs(amount));
        if (entry == null) {
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(entry.getValue());
    }
}
