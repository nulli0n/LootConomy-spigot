package su.nightexpress.lootconomy.hook.impl;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.lootconomy.booster.impl.Booster;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.Collection;

public class PlaceholderHook {

    private static Expansion expansion;

    public static void setup(@NotNull LootConomyPlugin plugin) {
        if (expansion == null) {
            expansion = new Expansion(plugin);
            expansion.register();
        }
    }

    public static void shutdown() {
        if (expansion != null) {
            expansion.unregister();
            expansion = null;
        }
    }

    private static class Expansion extends PlaceholderExpansion {

        private final LootConomyPlugin plugin;

        public Expansion(@NotNull LootConomyPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        @NotNull
        public String getAuthor() {
            return this.plugin.getDescription().getAuthors().get(0);
        }

        @Override
        @NotNull
        public String getIdentifier() {
            return this.plugin.getName().toLowerCase();
        }

        @Override
        @NotNull
        public String getVersion() {
            return this.plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            //LootUser user = this.plugin.getUserManager().getUserData(player);

            if (params.startsWith("currency_boost_multiplier_")) {
                String curId = params.substring("currency_boost_multiplier_".length());
                Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player);
                return NumberUtil.format(Booster.getMultiplier(curId, boosters));
            }

            if (params.startsWith("currency_boost_percent_")) {
                String curId = params.substring("currency_boost_percent_".length());
                Collection<Booster> boosters = plugin.getBoosterManager().getBoosters(player);
                return NumberUtil.format(Booster.getPercent(curId, boosters));
            }

            return null;
        }
    }
}
