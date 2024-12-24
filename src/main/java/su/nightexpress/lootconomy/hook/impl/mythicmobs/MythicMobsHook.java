package su.nightexpress.lootconomy.hook.impl.mythicmobs;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.lootconomy.api.loot.LootFormatter;
import su.nightexpress.lootconomy.api.loot.LootHandler;
import su.nightexpress.lootconomy.loot.handler.LootAction;
import su.nightexpress.lootconomy.loot.handler.LootActions;
import su.nightexpress.lootconomy.money.MoneyUtils;

public class MythicMobsHook {

    private static final MythicBukkit MYTHIC_MOBS = MythicBukkit.inst();

    public static final LootFormatter<MythicMob> FORMATTER = new LootFormatter<>() {

        @NotNull
        @Override
        public String getName(@NotNull MythicMob object) {
            return object.getInternalName();
        }

        @NotNull
        @Override
        public String getLocalized(@NotNull MythicMob object) {
            return object.getDisplayName().get();
        }

        @Nullable
        @Override
        public MythicMob parseObject(@NotNull String name) {
            return getMobConfig(name);
        }
    };

    public static final LootHandler<MythicMobDeathEvent, MythicMob> HANDLER = (plugin, event, provider) -> {
        LivingEntity killer = event.getKiller();

        if (!(killer instanceof Player player)) return false;
        if (MoneyUtils.isDevastated(event.getEntity())) return false;

        //ItemStack tool = player.getInventory().getItemInMainHand();
        MythicMob mythicMob = event.getMobType();
        //String type = mythicMob.getInternalName();

        provider.createLoot(plugin, player, mythicMob).forEach(itemStack -> {
            /*if (Plugins.isLoaded(HookId.EXCELLENT_ENCHANTS) && ExcellentEnchantsHook.hasNimble(tool)) {
                plugin.getMoneyManager().pickupMoney(player, item);
                return;
            }*/
            event.getDrops().add(itemStack);
        });
        return true;
    };

    public static final LootAction<MythicMobDeathEvent, MythicMob> ACTION = new LootAction<>("kill_mythic_mob", "mythic_mobs", FORMATTER, HANDLER);

    public static void register() {
        LootActions.register(ACTION);
    }

    public static boolean isMythicMob(@NotNull Entity entity) {
        return MYTHIC_MOBS.getAPIHelper().isMythicMob(entity);
    }

//    @Nullable
//    public static ActiveMob getMobInstance(@NotNull Entity entity) {
//        return MYTHIC_MOBS.getAPIHelper().getMythicMobInstance(entity);
//    }

//    @Nullable
//    public static MythicMob getMobConfig(@NotNull Entity entity) {
//        ActiveMob mob = getMobInstance(entity);
//        return mob != null ? mob.getType() : null;
//    }

    @Nullable
    public static MythicMob getMobConfig(@NotNull String mobId) {
        return MYTHIC_MOBS.getAPIHelper().getMythicMob(mobId);
    }

//    @NotNull
//    public static String getMobInternalName(@NotNull Entity entity) {
//        MythicMob mythicMob = getMobConfig(entity);
//        return mythicMob != null ? mythicMob.getInternalName() : "null";
//    }

//    @NotNull
//    public static String getMobDisplayName(@NotNull String mobId) {
//        MythicMob mythicMob = getMobConfig(mobId);
//        PlaceholderString string = mythicMob != null ? mythicMob.getDisplayName() : null;
//        return string != null ? string.get() : mobId;
//    }

//    public static double getMobLevel(@NotNull Entity entity) {
//        ActiveMob mob = getMobInstance(entity);
//        return mob != null ? mob.getLevel() : 0;
//    }

//    @NotNull
//    public static List<String> getMobConfigIds() {
//        return new ArrayList<>(MYTHIC_MOBS.getMobManager().getMobNames());
//    }

//    public static void killMob(@NotNull Entity entity) {
//        ActiveMob mob = getMobInstance(entity);
//        if (mob == null || mob.isDead()) return;
//
//        mob.setDead();
//        mob.remove();
//        entity.remove();
//    }

//    public static boolean isValid(@NotNull String mobId) {
//        return getMobConfig(mobId) != null;
//    }
}
