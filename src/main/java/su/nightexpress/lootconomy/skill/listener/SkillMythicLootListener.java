package su.nightexpress.lootconomy.skill.listener;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.lootconomy.hook.impl.ExcellentEnchantsHook;
import su.nightexpress.lootconomy.hook.impl.MythicMobsHook;
import su.nightexpress.lootconomy.money.MoneyManager;
import su.nightexpress.lootconomy.skill.SkillManager;
import su.nightexpress.lootconomy.skill.impl.SkillType;

public class SkillMythicLootListener extends AbstractListener<LootConomy> {

    public SkillMythicLootListener(@NotNull SkillManager skillManager) {
        super(skillManager.plugin());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSkillMythicKill(MythicMobDeathEvent event) {
        LivingEntity killer = event.getKiller();

        if (!(killer instanceof Player player)) return;
        if (MoneyManager.isDevastated(event.getEntity())) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        MythicMob mythicMob = event.getMobType();
        String type = mythicMob.getInternalName();

        this.plugin.getSkillManager().getSkillLoots(player, SkillType.KILL_MYTHIC_MOB, type).forEach(item -> {
            if (EngineUtils.hasPlugin(HookId.EXCELLENT_ENCHANTS) && ExcellentEnchantsHook.hasNimble(tool)) {
                this.plugin.getMoneyManager().pickupMoney(player, item);
                return;
            }
            MythicMobsHook.LOOT_HANDLER.handleLoot(event, player, item);
        });
    }
}
