package su.nightexpress.lootconomy.config;

import org.bukkit.NamespacedKey;
import su.nightexpress.lootconomy.LootConomyAPI;

public class Keys {

    public static final NamespacedKey LOOT_EMPTY  = new NamespacedKey(LootConomyAPI.PLUGIN, "loot.empty");

    public static final NamespacedKey ITEM_AMOUNT    = new NamespacedKey(LootConomyAPI.PLUGIN, "money.amount");
    public static final NamespacedKey ITEM_OWNER     = new NamespacedKey(LootConomyAPI.PLUGIN, "money.owner");
    public static final NamespacedKey ITEM_ID        = new NamespacedKey(LootConomyAPI.PLUGIN, "money.id");
    public static final NamespacedKey ITEM_SKILL     = new NamespacedKey(LootConomyAPI.PLUGIN, "money.skill");
    public static final NamespacedKey ITEM_OBJECTIVE = new NamespacedKey(LootConomyAPI.PLUGIN, "money.objective");
    public static final NamespacedKey ITEM_CURRENCY  = new NamespacedKey(LootConomyAPI.PLUGIN, "money.currency");

    public static final NamespacedKey SKILL_LEVEL_FIREWORK = new NamespacedKey(LootConomyAPI.PLUGIN, "skill.firework");
}
