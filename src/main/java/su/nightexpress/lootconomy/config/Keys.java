package su.nightexpress.lootconomy.config;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;

public class Keys {

    public static NamespacedKey entityNoLoot;

    public static NamespacedKey itemAmount;
    public static NamespacedKey itemOwner;
    public static NamespacedKey itemId;
    public static NamespacedKey itemObjective;
    public static NamespacedKey itemActionType;
    public static NamespacedKey itemCurrency;

    public static void load(@NotNull LootConomyPlugin plugin) {
        entityNoLoot = new NamespacedKey(plugin, "entity.no_loot");

        itemAmount = new NamespacedKey(plugin, "money.amount");
        itemOwner = new NamespacedKey(plugin, "money.owner");
        itemId = new NamespacedKey(plugin, "money.id");
        itemObjective = new NamespacedKey(plugin, "money.objective");
        itemActionType = new NamespacedKey(plugin, "money.action_type");
        itemCurrency = new NamespacedKey(plugin, "money.currency");
    }
}
