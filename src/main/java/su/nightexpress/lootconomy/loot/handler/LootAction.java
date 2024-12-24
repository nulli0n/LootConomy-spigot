package su.nightexpress.lootconomy.loot.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.api.loot.LootFormatter;
import su.nightexpress.lootconomy.api.loot.LootHandler;
import su.nightexpress.lootconomy.api.loot.LootProvider;
import su.nightexpress.lootconomy.money.MoneyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootAction<E extends Event, O> implements LootProvider<O> {

    private final String            id;
    private final String            defaultCategory;
    private final LootFormatter<O>  formatter;
    private final LootHandler<E, O> handler;

    public LootAction(@NotNull String id, @NotNull String defaultCategory, @NotNull LootFormatter<O> formatter, LootHandler<E, O> handler) {
        this.id = id.toLowerCase();
        this.defaultCategory = defaultCategory.toLowerCase();
        this.formatter = formatter;
        this.handler = handler;
    }

    @Override
    @NotNull
    public List<ItemStack> createLoot(@NotNull LootConomyPlugin plugin, @NotNull Player player, @NotNull O object) {
        if (!MoneyUtils.isMoneyAvailable(player)) return Collections.emptyList();

        String objectId = this.getObjectName(object);
        List<ItemStack> loot = new ArrayList<>();

        plugin.getMoneyManager().getObjectives(this, objectId).forEach(objective -> {
            loot.addAll(plugin.getMoneyManager().createLoot(player, objective));
        });

        return loot;
    }

    public boolean handle(@NotNull LootConomyPlugin plugin, @NotNull E event) {
        if (plugin.getMoneyManager().getObjectives(this).isEmpty()) return false;

        return this.handler.handle(plugin, event, this);
    }

    @Nullable
    public O parse(@NotNull String objectId) {
        return this.formatter.parseObject(objectId);
    }

    @NotNull
    public String getObjectName(@NotNull O object) {
        return this.formatter.getName(object).toLowerCase();
    }

    @NotNull
    public String getObjectName(@NotNull String object) {
        O parsed = this.parse(object);
        return parsed == null ? object : this.getObjectName(parsed);
    }

    @NotNull
    public String getObjectLocalizedName(@NotNull O object) {
        return this.formatter.getLocalized(object);
    }

    @NotNull
    public String getObjectLocalizedName(@NotNull String object) {
        O parsed = this.parse(object);
        return parsed == null ? object : this.getObjectLocalizedName(parsed);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getDefaultCategory() {
        return this.defaultCategory;
    }

    @NotNull
    public LootFormatter<O> getFormatter() {
        return this.formatter;
    }

    @NotNull
    public LootHandler<E, O> getHandler() {
        return this.handler;
    }
}
