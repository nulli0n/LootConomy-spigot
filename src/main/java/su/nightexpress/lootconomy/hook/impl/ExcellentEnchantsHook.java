package su.nightexpress.lootconomy.hook.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.enchantment.impl.tool.TelekinesisEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.weapon.NimbleEnchant;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class ExcellentEnchantsHook {

    public static boolean hasTelekinesis(@NotNull ItemStack tool) {
        EnchantmentData data = EnchantRegistry.getById(TelekinesisEnchant.ID);
        return data != null && EnchantUtils.contains(tool, data.getEnchantment());
    }

    public static boolean hasNimble(@NotNull ItemStack tool) {
        EnchantmentData data = EnchantRegistry.getById(NimbleEnchant.ID);
        return data != null && EnchantUtils.contains(tool, data.getEnchantment());
    }
}
