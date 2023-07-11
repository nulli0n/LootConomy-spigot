package su.nightexpress.lootconomy.skill.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.playerblocktracker.PlayerBlockTracker;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Keys;
import su.nightexpress.lootconomy.hook.HookId;
import su.nightexpress.lootconomy.hook.impl.ExcellentEnchantsHook;
import su.nightexpress.lootconomy.hook.impl.MythicMobsHook;
import su.nightexpress.lootconomy.money.MoneyManager;
import su.nightexpress.lootconomy.skill.SkillManager;
import su.nightexpress.lootconomy.skill.impl.SkillType;
import su.nightexpress.lootconomy.skill.util.LootHandler;

public class SkillVanillaLootListener extends AbstractListener<LootConomy> {

    public SkillVanillaLootListener(@NotNull SkillManager skillManager) {
        super(skillManager.plugin());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSkillFireworkDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Firework firework)) return;
        if (PDCUtil.getBoolean(firework, Keys.SKILL_LEVEL_FIREWORK).orElse(false)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSkillMobKill(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead instanceof Player) return;
        if (MoneyManager.isDevastated(dead)) return;
        if (EngineUtils.hasPlugin(HookId.MYTHIC_MOBS) && MythicMobsHook.isMythicMob(dead)) return;

        Player player = dead.getKiller();
        if (player == null) return;

        ItemStack tool = player.getInventory().getItemInMainHand();

        String type = dead.getType().name();
        this.plugin.getSkillManager().getSkillLoots(player, SkillType.KILL_MOB, type).forEach(item -> {
            if (EngineUtils.hasPlugin(HookId.EXCELLENT_ENCHANTS) && ExcellentEnchantsHook.hasNimble(tool)) {
                this.plugin.getMoneyManager().pickupMoney(player, item);
                return;
            }
            LootHandler.KILL_MOB.handleLoot(event, player, item);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkillFishing(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        //if (!e.getHook().isInOpenWater()) return;

        Entity caught = event.getCaught();
        if (caught == null) return;

        String type = caught.getType().name();
        if (caught instanceof Item item) {
            type = item.getItemStack().getType().name();
        }

        String objectiveName = type;
        Player player = event.getPlayer();
        this.plugin.getSkillManager().getSkillLoots(player, SkillType.FISHING, type).forEach(item -> {
            LootHandler.FISHING.handleLoot(event, player, item);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkillBlockHarvest(PlayerHarvestBlockEvent e) {
        Block block = e.getHarvestedBlock();
        if (PlayerBlockTracker.isTracked(block)) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        String type = block.getType().name();

        BlockBreakEvent event = new BlockBreakEvent(block, player);
        this.plugin.getSkillManager().getSkillLoots(player, SkillType.BLOCK_BREAK, type).forEach(item -> {
            if (EngineUtils.hasPlugin(HookId.EXCELLENT_ENCHANTS) && ExcellentEnchantsHook.hasTelekinesis(tool)) {
                this.plugin.getMoneyManager().pickupMoney(player, item);
                return;
            }
            LootHandler.BLOCK_BREAK.handleLoot(event, player, item);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSkillBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        BlockData blockData = block.getBlockData();
        Material blockType = block.getType();
        boolean isTall = blockType == Material.BAMBOO || blockType == Material.SUGAR_CANE;

        // Do not give money for ungrowth plants.
        if (!isTall && blockData instanceof Ageable age) {
            if (age.getAge() < age.getMaximumAge()) return;
        }

        Player player = e.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        String type = blockType.name();
        int blockHeight = isTall ? (blockType == Material.BAMBOO ? 16 : 4) : 1;
        for (int currentHeight = 0; currentHeight < blockHeight; currentHeight++) {
            if (currentHeight > 0) {
                block = block.getRelative(BlockFace.UP);
                if (block.getType() != blockType) break;
            }

            if (PlayerBlockTracker.isTracked(block)) {
                //PlayerBlockTracker.unTrack(block);
                continue;
            }

            BlockBreakEvent event = new BlockBreakEvent(block, player);
            this.plugin.getSkillManager().getSkillLoots(player, SkillType.BLOCK_BREAK, type).forEach(item -> {
                if (EngineUtils.hasPlugin(HookId.EXCELLENT_ENCHANTS) && ExcellentEnchantsHook.hasTelekinesis(tool)) {
                    this.plugin.getMoneyManager().pickupMoney(player, item);
                    return;
                }
                LootHandler.BLOCK_BREAK.handleLoot(event, player, item);
            });
        }
    }
}
