package su.nightexpress.lootconomy.skill;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.FileUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.random.Rnd;
import su.nexmedia.playerblocktracker.PlayerBlockTracker;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.api.event.*;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Keys;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.lootconomy.money.MoneyManager;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.skill.impl.SkillObjective;
import su.nightexpress.lootconomy.skill.impl.SkillType;
import su.nightexpress.lootconomy.skill.listener.SkillMythicLootListener;
import su.nightexpress.lootconomy.skill.listener.SkillVanillaLootListener;
import su.nightexpress.lootconomy.skill.menu.SkillListMenu;
import su.nightexpress.lootconomy.skill.menu.SkillResetMenu;
import su.nightexpress.lootconomy.skill.task.UpdateTopTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SkillManager extends AbstractManager<LootConomy> {

    public static final String DIR_SKILLS = "/skills/";

    private SkillListMenu  skillListMenu;
    private SkillResetMenu skillResetMenu;
    private UpdateTopTask updateTopTask;

    private final Map<String, Skill>                 skillMap;
    private final Map<Skill, List<Pair<String, Integer>>> topLevelMap;
    private final Predicate<Block>                   skillBlockFilter;

    public SkillManager(@NotNull LootConomy plugin) {
        super(plugin);
        this.skillMap = new HashMap<>();
        this.topLevelMap = new ConcurrentHashMap<>();
        this.skillBlockFilter = (block) -> {
            // сраный бамбук из саженца можно превратить в палку поставив сверху еще один. и ни одного ивента на это нет
            Material type = block.getType();
            if (type == Material.BAMBOO_SAPLING) type = Material.BAMBOO;

            Skill skill = this.getSkillByType(SkillType.BLOCK_BREAK, type.name());
            return skill != null;
        };
    }

    @Override
    protected void onLoad() {
        this.plugin.getConfigManager().extractResources(DIR_SKILLS);

        FileUtil.getFolders(plugin.getDataFolder() + DIR_SKILLS).forEach(jobDir -> {
            JYML cfg = JYML.loadOrExtract(plugin, DIR_SKILLS + jobDir.getName(), "settings.yml");
            Skill job = new Skill(plugin, cfg, jobDir.getName());
            if (job.load()) {
                this.skillMap.put(job.getId(), job);
            }
            else this.plugin.warn("Job not loaded: '" + jobDir.getName() + "' !");
        });
        this.plugin.info("Loaded " + this.getSkillMap().size() + " jobs.");

        this.skillListMenu = new SkillListMenu(this.plugin);
        this.skillResetMenu = new SkillResetMenu(this.plugin);

        PlayerBlockTracker.BLOCK_FILTERS.add(this.skillBlockFilter);

        this.addListener(new SkillVanillaLootListener(this));
        if (Hooks.hasPlugin(HookId.MYTHIC_MOBS)) {
            this.addListener(new SkillMythicLootListener(this));
        }

        this.updateTopTask = new UpdateTopTask(this.plugin);
        this.updateTopTask.start();

        //LazyGen.generate();
    }

    @Override
    protected void onShutdown() {
        if (this.updateTopTask != null) this.updateTopTask.stop();
        if (this.skillListMenu != null) this.skillListMenu.clear();
        if (this.skillResetMenu != null) this.skillResetMenu.clear();
        PlayerBlockTracker.BLOCK_FILTERS.remove(this.skillBlockFilter);
        this.getSkills().forEach(Skill::clear);
        this.getSkillMap().clear();
    }

    @NotNull
    public SkillListMenu getSkillListMenu() {
        return skillListMenu;
    }

    @NotNull
    public SkillResetMenu getSkillResetMenu() {
        return skillResetMenu;
    }

    @NotNull
    public Map<Skill, List<Pair<String, Integer>>> getTopLevelMap() {
        return topLevelMap;
    }

    @NotNull
    public Map<String, Skill> getSkillMap() {
        return skillMap;
    }

    @NotNull
    public Collection<Skill> getSkills() {
        return this.getSkillMap().values();
    }

    @Nullable
    public Skill getSkillById(@NotNull String id) {
        return this.getSkillMap().get(id.toLowerCase());
    }

    @Nullable
    public Skill getSkillByType(@NotNull SkillType type, @NotNull String objective) {
        return this.getSkills().stream().filter(job -> job.getType() == type && job.hasObjective(objective)).findFirst().orElse(null);
    }

    @NotNull
    public Collection<Skill> getSkills(@NotNull Player player) {
        return this.getSkills().stream().filter(job -> job.hasPermission(player)).collect(Collectors.toSet());
    }

    @NotNull
    public Collection<Skill> getSkills(@NotNull SkillType type) {
        return this.getSkills().stream().filter(job -> job.getType() == type).collect(Collectors.toSet());
    }

    @NotNull
    public List<ItemStack> getSkillLoots(@NotNull Player player, @NotNull SkillType type, @NotNull String object) {
        List<ItemStack> loot = new ArrayList<>();
        this.getSkills(type).forEach(skill -> {
            loot.addAll(this.getSkillLoot(player, skill, object));
        });
        return loot;
    }

    @NotNull
    public List<ItemStack> getSkillLoot(@NotNull Player player, @NotNull Skill skill, @NotNull String object) {
        List<ItemStack> loot = new ArrayList<>();

        if (!skill.hasPermission(player)) return loot;
        if (!MoneyManager.isMoneyAvailable(player)) return loot;

        LootUser user = plugin.getUserManager().getUserData(player);
        SkillData skillData = user.getData(skill);

        SkillObjective objective = skill.getObjective(object);
        if (objective == null || !objective.canDrop() || !objective.isUnlocked(player, skillData)) return loot;

        Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player, skill);

        for (Currency currency : plugin.getCurrencyManager().getCurrencies()) {
            double amount = objective.getCurrencyDrop(currency).rollAmountNaturally();
            // Apply boosters and job modifiers only when objective returns positive amount of money
            // to prevent money multiply in negative way.
            if (amount > 0D) {
                if (skillData.isCurrencyLimitReached(currency)) {
                    amount = 0D;
                }
                else {
                    amount *= Booster.getCurrencyBoost(currency, boosters);
                    amount *= skillData.getRank().getCurrencyMultiplier(currency, skillData.getLevel());
                }
            }

            if (currency.round(amount) == 0D) continue;
            if (amount < 0D/* || currency.isDirectToBalance()*/) { // TODO Perk AutoPickup
                this.plugin.getMoneyManager().pickupMoney(player, currency, amount, skill, objective);
            }
            else {
                PlayerCurrencyLootCreateEvent event = new PlayerCurrencyLootCreateEvent(player, currency, amount, skill, objective);
                plugin.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    ItemStack moneyItem = MoneyManager.createMoney(event.getCurrency(), event.getAmount(), player, event.getSkill(), event.getObjective());
                    loot.add(moneyItem);
                }
            }

            if (!player.hasPermission(Perms.BYPASS_SKILL_LIMIT_CURRENCY) && skillData.getRank().isCurrencyLimited(currency, skillData.getLevel())) {
                skillData.getLimitData().addCurrency(currency, amount);

                if (skillData.isCurrencyLimitReached(currency)) {
                    this.plugin.getMessage(Lang.SKILL_LIMIT_CURRENCY_NOTIFY)
                        .replace(skill.replacePlaceholders())
                        .replace(currency.replacePlaceholders())
                        .send(player);
                }
            }
        }

        double expRoll = objective.getXPDrop().rollAmountNaturally();
        if (expRoll > 0D) {
            if (skillData.isXPLimitReached()) {
                expRoll = 0D;
            }
            else {
                expRoll *= Booster.getXPBoost(boosters);
                expRoll *= skillData.getRank().getXPMultiplier(skillData.getLevel());
            }
        }

        if (expRoll != 0 && Config.LEVELING_ENABLED.get()) {
            if (this.addXP(player, skill, objective.getName(), (int) expRoll, false)) {
                if (!player.hasPermission(Perms.BYPASS_SKILL_LIMIT_XP) && skillData.getRank().isXPLimited(skillData.getLevel())) {
                    skillData.getLimitData().addXP((int) expRoll);

                    if (skillData.isXPLimitReached()) {
                        this.plugin.getMessage(Lang.SKILL_LIMIT_XP_NOTIFY)
                            .replace(skill.replacePlaceholders())
                            .send(player);
                    }
                }
            }
        }

        return loot;
    }

    public void addLevel(@NotNull Player player, @NotNull Skill skill, int amount) {
        LootUser user = plugin.getUserManager().getUserData(player);
        SkillData skillData = user.getData(skill);
        boolean isMinus = amount < 0;

        for (int count = 0; count < Math.abs(amount); count++) {
            int exp = isMinus ? -skillData.getXPToLevelDown() : skillData.getXPToLevelUp();
            this.addXP(player, skill, exp, false);
        }
    }

    public boolean addXP(@NotNull Player player, @NotNull Skill skill, int amount) {
        return this.addXP(player, skill, amount, false);
    }

    public boolean addXP(@NotNull Player player, @NotNull Skill skill, int amount, boolean useBooster) {
        return this.addXP(player, skill, "", amount, useBooster);
    }

    public boolean addXP(@NotNull Player player, @NotNull Skill skill, @NotNull String source, int amount, boolean useBooster) {
        if (amount == 0) return false;

        LootUser user = plugin.getUserManager().getUserData(player);
        SkillData skillData = user.getData(skill);
        Collection<Booster> boosters = this.plugin.getBoosterManager().getBoosters(player, skill);

        if (useBooster && amount > 0D) amount *= Booster.getXPBoost(boosters);
        boolean isLose = amount < 0;

        PlayerSkillXPEvent event;
        if (isLose) {
            event = new PlayerSkillXPLoseEvent(player, user, skillData, source, amount);
        }
        else {
            event = new PlayerSkillXPGainEvent(player, user, skillData, source, amount);
        }
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        amount = event.getXP();

        int levelHas = skillData.getLevel();
        if (isLose) {
            skillData.removeExp(amount);
        }
        else {
            skillData.addExp(amount);
        }

        // Send exp gain/lose message.
        plugin.getMessage(isLose ? Lang.SKILL_XP_LOSE : Lang.SKILL_XP_GAIN)
            .replace(skillData.replacePlaceholders())
            .replace(Placeholders.GENERIC_AMOUNT, amount)
            .send(player);

        // Call events for level up/down.
        if (levelHas > skillData.getLevel()) {
            PlayerSkillLevelDownEvent levelDownEvent = new PlayerSkillLevelDownEvent(player, user, skillData);
            plugin.getPluginManager().callEvent(levelDownEvent);

            plugin.getMessage(Lang.SKILL_LEVEL_DOWN)
                .replace(skillData.replacePlaceholders())
                .send(player);
        }
        else if (levelHas < skillData.getLevel()) {
            PlayerSkillLevelUpEvent levelUpEvent = new PlayerSkillLevelUpEvent(player, user, skillData);
            plugin.getPluginManager().callEvent(levelUpEvent);

            skillData.getRank().getLevelUpCommands(skillData.getLevel()).forEach(command -> {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), Placeholders.Player.replacer(player).apply(command));
            });

            plugin.getMessage(Lang.SKILL_LEVEL_UP)
                .replace(skillData.replacePlaceholders())
                .send(player);

            if (Config.LEVELING_FIREWORKS.get()) {
                this.createFirework(player.getWorld(), player.getLocation());
            }
        }
        return true;
    }

    @NotNull
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
    }
}
