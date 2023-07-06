package su.nightexpress.lootconomy.hook.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.tool.EnchantTelekinesis;
import su.nightexpress.excellentenchants.enchantment.impl.weapon.EnchantNimble;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class ExcellentEnchantsHook {

    public static boolean hasTelekinesis(@NotNull ItemStack tool) {
        ExcellentEnchant enchant = EnchantRegistry.getById(EnchantTelekinesis.ID);
        return enchant != null && EnchantUtils.contains(tool, enchant);
    }

    public static boolean hasNimble(@NotNull ItemStack tool) {
        ExcellentEnchant enchant = EnchantRegistry.getById(EnchantNimble.ID);
        return enchant != null && EnchantUtils.contains(tool, enchant);
    }
}
