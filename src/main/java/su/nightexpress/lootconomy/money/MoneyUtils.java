package su.nightexpress.lootconomy.money;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.config.Config;
import su.nightexpress.lootconomy.config.Keys;
import su.nightexpress.lootconomy.money.object.MoneyObjective;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.UUID;

public class MoneyUtils {

    @SuppressWarnings("UnstableApiUsage")
    @Nullable
    public static Entity getDamager(@NotNull EntityDamageEvent event) {
        if (Version.isAtLeast(Version.V1_20_R3)) {
            return event.getDamageSource().getCausingEntity();
        }

        if (event instanceof EntityDamageByEntityEvent ede) {
            Entity damager = ede.getDamager();
            if (damager instanceof LivingEntity living) {
                return living;
            }
            else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity living) {
                return living;
            }
            return damager;
        }

        return null;
    }

    @NotNull
    public static String getCustomMultiplier(@NotNull String name) {
        return Config.OBJECTIVE_CUSTOM_MULTIPLIERS_LIST.get().getOrDefault(name.toLowerCase(), "");
    }

    public static double parseCustomMultiplier(@NotNull Player player, @NotNull String name) {
        if (!Plugins.hasPlaceholderAPI()) return 1D;

        String multiplier = PlaceholderAPI.setPlaceholders(player, getCustomMultiplier(name));
        return NumberUtil.parseDouble(multiplier).orElse(1D);
    }

    public static boolean isMoneyAvailable(@NotNull Player player) {
        if (!Players.isReal(player) || MoneyUtils.isDisabledGamemode(player.getGameMode())) return false;

        return !MoneyUtils.isDisabledWorld(player.getWorld());
    }

    @NotNull
    public static ItemStack createMoney(@NotNull Currency currency, double amount) {
        return createMoney(currency, amount, null, null);
    }

    @NotNull
    public static ItemStack createMoney(@NotNull Currency currency, double amount, @Nullable UUID ownerId) {
        return createMoney(currency, amount, ownerId, null);
    }

    @NotNull
    public static ItemStack createMoney(@NotNull Currency currency, double amount, @Nullable UUID ownerId, @Nullable MoneyObjective objective) {
        if (amount == 0D) throw new IllegalArgumentException("Money amount can not be zero!");

        // Get item model depends on the money amount.
        ItemStack item = currency.getIcon(amount);

        double finalMoney = currency.round(amount);
        ItemUtil.editMeta(item, meta -> {
            PDCUtil.set(meta, Keys.itemAmount, finalMoney);
            PDCUtil.set(meta, Keys.itemCurrency, currency.getId());
            PDCUtil.set(meta, Keys.itemId, UUID.randomUUID().toString());
            if (objective != null) {
                PDCUtil.set(meta, Keys.itemObjective, objective.getId());
                PDCUtil.set(meta, Keys.itemActionType, objective.getActionType().getName());
            }
            if (Config.LOOT_PROTECTION.get() && ownerId != null) {
                PDCUtil.set(meta, Keys.itemOwner, ownerId);
            }

            meta.setDisplayName(NightMessage.asLegacy(currency.dropFormat(amount)));
        });
        return item;
    }

    public static boolean isDisabledWorld(@NotNull World world) {
        return isDisabledWorld(world.getName());
    }

    public static boolean isDisabledWorld(@NotNull String worldName) {
        return Config.DISABLED_WORLDS.get().contains(worldName);
    }

    public static boolean isDisabledGamemode(@NotNull GameMode gameMode) {
        return Config.ABUSE_IGNORE_GAME_MODES.get().contains(gameMode);
    }

    public static boolean isMoney(@NotNull ItemStack item) {
        return PDCUtil.getDouble(item, Keys.itemAmount).isPresent();
    }

    public static boolean isOwner(@NotNull ItemStack item, @NotNull Player player) {
        UUID ownerId = getOwnerId(item);
        return ownerId == null || player.getUniqueId().equals(ownerId);
    }

    @Nullable
    public static UUID getOwnerId(@NotNull ItemStack item) {
        return PDCUtil.getUUID(item, Keys.itemOwner).orElse(null);
    }

    @Nullable
    public static String getObjectiveId(@NotNull ItemStack item) {
        return PDCUtil.getString(item, Keys.itemObjective).orElse(null);
    }

    @Nullable
    public static String getActionName(@NotNull ItemStack item) {
        return PDCUtil.getString(item, Keys.itemActionType).orElse(null);
    }

    @Nullable
    public static String getCurrencyId(@NotNull ItemStack item) {
        return PDCUtil.getString(item, Keys.itemCurrency).orElse(null);
    }

    public static double getMoneyAmount(@NotNull ItemStack item) {
        return PDCUtil.getDouble(item, Keys.itemAmount).orElse(0D);
    }

    public static void devastateEntity(@NotNull Entity entity) {
        PDCUtil.set(entity, Keys.entityNoLoot, true);
    }

    public static boolean isDevastated(@NotNull Entity entity) {
        return PDCUtil.getBoolean(entity, Keys.entityNoLoot).isPresent();
    }
}
