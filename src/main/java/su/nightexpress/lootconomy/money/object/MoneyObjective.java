package su.nightexpress.lootconomy.money.object;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.action.ActionType;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MoneyObjective {

    private final ActionType<?, ?>      actionType;
    private final String                id;
    private final String                displayName;
    private final ItemStack             icon;
    private final Set<String>           objects;
    private final Map<String, DropInfo> currencyDrops;

    public MoneyObjective(@NotNull ActionType<?, ?> actionType,
                          @NotNull String id,
                          @NotNull String displayName,
                          @NotNull ItemStack icon,
                          @NotNull Set<String> objects,
                          @NotNull Map<String, DropInfo> currencyDrops) {
        this.actionType = actionType;
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.icon = icon;
        this.objects = objects;
        this.currencyDrops = currencyDrops;
    }

    @Nullable
    public static MoneyObjective read(@NotNull LootConomyPlugin plugin, @NotNull FileConfig config, @NotNull String path, @NotNull String name) {
        //if (!cfg.getBoolean(path + ".Enabled")) return null;

        ActionType<?, ?> actionType = plugin.getActionRegistry().getActionType(ConfigValue.create(path + ".ActionType", "null").read(config));
        if (actionType == null) {
            plugin.error("Invalid 'ActionType' for '" + name + "' objective (File: '" + config.getFile().getName() + "').");
            return null;
        }

        // Add missing currencies for users to know they can use them.
        plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
            if (!config.contains(path + ".Drops.Currency." + currency.getId())) {
                DropInfo.EMPTY.write(config, path + ".Drops.Currency." + currency.getId());
            }
        });

        String displayName = config.getString(path + ".Display_Name", name);
        ItemStack icon = config.getItem(path + ".Icon");

        Set<String> objects = new HashSet<>();
        ConfigValue.create(path + ".Objects", Lists.newSet()).read(config).forEach(raw -> {
            if (actionType.getObjectFormatter().parseObject(raw) == null) {
                plugin.error("Unknown object '" + raw + "' in '" + name + "' objective (File: '" + config.getFile().getName() + "').");
                return;
            }
            objects.add(raw.toLowerCase());
        });

        Map<String, DropInfo> currencyDrop = new HashMap<>();
        for (String curId : config.getSection(path + ".Drops.Currency")) {
            DropInfo dropInfo = DropInfo.read(config, path + ".Drops.Currency." + curId);
            currencyDrop.put(curId, dropInfo);
        }

        return new MoneyObjective(actionType, name, displayName, icon, objects, currencyDrop);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".ActionType", this.actionType.getName());
        config.set(path + ".Display_Name", this.getDisplayName());
        config.setItem(path + ".Icon", this.getIcon());
        config.set(path + ".Objects", this.objects);

        this.getCurrencyDrops().forEach((id, dropInfo) -> {
            dropInfo.write(config, path + ".Drops.Currency." + id);
        });
    }

    public boolean hasObject(@NotNull String object) {
        return this.objects.contains(object.toLowerCase());
    }

    public boolean canDrop() {
        return this.currencyDrops.values().stream().anyMatch(Predicate.not(DropInfo::isEmpty));
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
    public ActionType<?, ?> getActionType() {
        return actionType;
    }

    @NotNull
    public String getId() {
        return id;
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
    public Set<String> getObjects() {
        return objects;
    }

    @NotNull
    public Map<String, DropInfo> getCurrencyDrops() {
        return currencyDrops;
    }
}
