package su.nightexpress.lootconomy.action;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface LootProcessor<O> {

    @NotNull List<ItemStack> getLoot(@NotNull Player player, @NotNull O object, int amount);
}
