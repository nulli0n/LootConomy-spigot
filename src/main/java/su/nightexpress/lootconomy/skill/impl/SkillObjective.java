package su.nightexpress.lootconomy.skill.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Colorizer;
import su.nightexpress.lootconomy.LootConomyAPI;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.skill.util.DropInfo;

import java.util.HashMap;
import java.util.Map;

public class SkillObjective {

    private final String                name;
    private final String displayName;
    private final ItemStack icon;
    private final Map<String, DropInfo> currencyDrops;
    private final DropInfo              xpDrops;
    private final int unlockLevel;

    public SkillObjective(@NotNull String name, @NotNull String displayName, @NotNull ItemStack icon,
                          @NotNull Map<String, DropInfo> currencyDrops, @NotNull DropInfo xpDrops,
                          int unlockLevel) {
        this.name = name.toLowerCase();
        this.displayName = displayName;
        this.icon = icon;
        this.currencyDrops = currencyDrops;
        this.xpDrops = xpDrops;
        this.unlockLevel = unlockLevel;
    }

    @Nullable
    public static SkillObjective read(@NotNull JYML cfg, @NotNull String path, @NotNull String name) {
        //if (!cfg.getBoolean(path + ".Enabled")) return null;

        // Add missing currencies for users to know they can use them.
        LootConomyAPI.PLUGIN.getCurrencyManager().getCurrencies().forEach(currency -> {
            if (!cfg.contains(path + ".Drops.Currency." + currency.getId())) {
                DropInfo.EMPTY.write(cfg, path + ".Drops.Currency." + currency.getId());
            }
        });
        cfg.saveChanges();

        String displayName = Colorizer.apply(cfg.getString(path + ".Display_Name", name));
        ItemStack icon = cfg.getItem(path + ".Icon");

        Map<String, DropInfo> currencyDrop = new HashMap<>();
        for (String curId : cfg.getSection(path + ".Drops.Currency")) {
            DropInfo dropInfo = DropInfo.read(cfg, path + ".Drops.Currency." + curId);
            currencyDrop.put(curId, dropInfo);
        }
        DropInfo xpDrop = DropInfo.read(cfg, path + ".Drops.XP");

        int unlockLevel = cfg.getInt(path + ".Unlock_Level");

        return new SkillObjective(name, displayName, icon, currencyDrop, xpDrop, unlockLevel);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Display_Name", this.getDisplayName());
        cfg.setItem(path + ".Icon", this.getIcon());

        this.getCurrencyDrops().forEach((id, dropInfo) -> {
            dropInfo.write(cfg, path + ".Drops.Currency." + id);
        });
        this.getXPDrop().write(cfg, path + ".Drops.XP");

        cfg.set(path + ".Unlock_Level", this.getUnlockLevel());
    }

    public boolean isUnlocked(@NotNull Player player, @NotNull SkillData skillData) {
        if (!player.hasPermission(Perms.BYPASS_OBJECTIVE_UNLOCK_LEVEL) && !this.isUnlocked(skillData.getLevel())) {
            return false;
        }
        return true;
    }

    public boolean isUnlocked(int skillLevel) {
        return skillLevel >= this.getUnlockLevel();
    }

    public boolean canDrop() {
        return !this.getCurrencyDrops().values().stream().allMatch(DropInfo::isEmpty) && !this.getXPDrop().isEmpty();
    }

    @NotNull
    public DropInfo getCurrencyDrop(@NotNull Currency currency) {
        return this.getCurrencyDrop(currency.getId());
    }

    @NotNull
    public DropInfo getCurrencyDrop(@NotNull String id) {
        return this.getCurrencyDrops().getOrDefault(id.toLowerCase(), DropInfo.EMPTY);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public ItemStack getIcon() {
        return new ItemStack(icon);
    }

    @NotNull
    public Map<String, DropInfo> getCurrencyDrops() {
        return currencyDrops;
    }

    @NotNull
    public DropInfo getXPDrop() {
        return xpDrops;
    }

    public int getUnlockLevel() {
        return unlockLevel;
    }
}
