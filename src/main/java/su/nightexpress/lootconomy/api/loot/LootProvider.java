package su.nightexpress.lootconomy.api.loot;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;

import java.util.List;

public interface LootProvider<O> {

    @NotNull List<ItemStack> createLoot(@NotNull LootConomyPlugin plugin, @NotNull Player player, @NotNull O object);
}
