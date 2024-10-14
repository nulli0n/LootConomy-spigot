package su.nightexpress.lootconomy.hook.impl;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.BukkitThing;

public class ExcellentEnchantsHook {

//    public static boolean hasTelekinesis(@NotNull ItemStack tool) {
//        CustomEnchantment enchantment = EnchantRegistry.getById(TelekinesisEnchant.ID);
//        return enchantment != null && EnchantUtils.contains(tool, enchantment.getBukkitEnchantment());
//    }

    public static boolean hasNimble(@NotNull ItemStack tool) {
        Enchantment enchantment = BukkitThing.getEnchantment("nimble");
        return enchantment != null &&  tool.getEnchantmentLevel(enchantment) != 0;
    }

    public static boolean hasTelekinesis(@NotNull ItemStack tool) {
        Enchantment enchantment = BukkitThing.getEnchantment("telekinesis");
        return enchantment != null &&  tool.getEnchantmentLevel(enchantment) != 0;
    }
}
