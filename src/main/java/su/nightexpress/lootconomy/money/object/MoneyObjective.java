package su.nightexpress.lootconomy.money.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.lootconomy.loot.objective.ObjectiveCategory;
import su.nightexpress.lootconomy.money.MoneyUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MoneyObjective {

    private final String                id;
    private final String                actionName;
    private final String                category;
    private final String      displayName;
    private final NightItem   icon;
    private final Set<String> objects;
    private final Map<String, DropInfo> currencyDrops;

    public MoneyObjective(@NotNull String id,
                          @NotNull String actionName,
                          @NotNull String category,
                          @NotNull String displayName,
                          @NotNull NightItem icon,
                          @NotNull Set<String> objects,
                          @NotNull Map<String, DropInfo> currencyDrops) {
        this.actionName = actionName.toLowerCase();
        this.category = category.toLowerCase();
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.icon = icon;
        this.objects = objects;
        this.currencyDrops = currencyDrops;
    }

    @NotNull
    public static MoneyObjective read(/*@NotNull LootConomyPlugin plugin, */@NotNull FileConfig config, @NotNull String path, @NotNull String id) {
        //if (!cfg.getBoolean(path + ".Enabled")) return null;

        String actionType = ConfigValue.create(path + ".ActionType", "null").read(config);
//        var action = LootActions.getByName(actionType);
//
//        if (action == null) {
//            plugin.error("Invalid 'ActionType' for '" + id + "' objective (File: '" + config.getFile().getName() + "').");
//            return null;
//        }

        // Add missing currencies for users to know they can use them.
//        plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
//            if (!config.contains(path + ".Drops.Currency." + currency.getId())) {
//                DropInfo.EMPTY.write(config, path + ".Drops.Currency." + currency.getId());
//            }
//        });

        String category = ConfigValue.create(path + ".Category", MoneyUtils.getDefaultActionCategory(actionType)).read(config);
//        if (!plugin.getMoneyManager().hasCategory(category)) {
//            plugin.warn("Invalid 'Category' for '" + id + "' objective (File: '" + config.getFile().getName() + "').");
//        }

        String displayName = config.getString(path + ".Display_Name", id);
        NightItem icon = config.getCosmeticItem(path + ".Icon");

        Set<String> objects = ConfigValue.create(path + ".Objects", Lists.newSet()).onRead(set -> Lists.modify(set, String::toLowerCase)).read(config);//new HashSet<>();
//        ConfigValue.create(path + ".Objects", Lists.newSet()).read(config).forEach(raw -> {
//            if (action.parse(raw) == null) {
//                plugin.error("Unknown object '" + raw + "' in '" + id + "' objective (File: '" + config.getFile().getName() + "').");
//                return;
//            }
//            objects.add(raw.toLowerCase());
//        });

        Map<String, DropInfo> currencyDrop = new HashMap<>();
        for (String curId : config.getSection(path + ".Drops.Currency")) {
            DropInfo dropInfo = DropInfo.read(config, path + ".Drops.Currency." + curId);
            currencyDrop.put(CurrencyId.reroute(curId), dropInfo);
        }

        return new MoneyObjective(id, actionType, category, displayName, icon, objects, currencyDrop);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".ActionType", this.actionName);
        config.set(path + ".Display_Name", this.displayName);
        config.set(path + ".Icon", this.icon);
        config.set(path + ".Objects", this.objects);

        this.currencyDrops.forEach((id, dropInfo) -> {
            dropInfo.write(config, path + ".Drops.Currency." + id);
        });
    }

    public boolean isCategory(@NotNull ObjectiveCategory category) {
        return this.category.equalsIgnoreCase(category.getId());
    }

    public boolean hasObject(@NotNull String object) {
        return this.objects.contains(object.toLowerCase());
    }

    public boolean canDrop() {
        return this.currencyDrops.values().stream().anyMatch(Predicate.not(DropInfo::isEmpty));
    }

    @NotNull
    public DropInfo getCurrencyDrop(@NotNull Currency currency) {
        return this.getCurrencyDrop(currency.getInternalId());
    }

    @NotNull
    public DropInfo getCurrencyDrop(@NotNull String id) {
        return this.currencyDrops.getOrDefault(id.toLowerCase(), DropInfo.EMPTY);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getActionName() {
        return this.actionName;
    }

    @NotNull
    public String getCategory() {
        return this.category;
    }

    @NotNull
    public String getDisplayName() {
        return this.displayName;
    }

    @NotNull
    public NightItem getIcon() {
        return this.icon.copy();
    }

    @NotNull
    public Set<String> getObjects() {
        return this.objects;
    }

    @NotNull
    public Map<String, DropInfo> getCurrencyDrops() {
        return this.currencyDrops;
    }
}
