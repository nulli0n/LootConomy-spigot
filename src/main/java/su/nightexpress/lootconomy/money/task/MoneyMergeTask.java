package su.nightexpress.lootconomy.money.task;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.money.MoneyManager;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.skill.impl.SkillObjective;

public class MoneyMergeTask extends AbstractTask<LootConomy> {

    private final MoneyManager moneyManager;

    public MoneyMergeTask(@NotNull MoneyManager moneyManager) {
        super(moneyManager.plugin(), 2, false);
        this.moneyManager = moneyManager;
    }

    @Override
    public void action() {
        this.moneyManager.getTrackedLoot().stream().filter(Item::isOnGround).forEach(item -> {
            if (!item.isValid() || item.isDead()) return; // This is needed to due 'near' item removal.

            ItemStack stackSrc = item.getItemStack();
            Currency currencySrc = MoneyManager.getMoneyCurrency(stackSrc);
            if (currencySrc == null) {
                item.remove();
                return;
            }

            String ownerSrc = MoneyManager.getMoneyOwner(stackSrc);
            Skill skillSrc = MoneyManager.getMoneyJob(stackSrc);
            SkillObjective objectiveSrc = MoneyManager.getMoneyObjective(stackSrc);
            double moneySrc = MoneyManager.getMoneyAmount(stackSrc);

            for (Entity near : item.getNearbyEntities(5, 1, 5)) {
                if (!(near instanceof Item nearItem)) continue;

                ItemStack stackNear = nearItem.getItemStack();
                if (!MoneyManager.isMoneyItem(stackNear)) continue;

                String ownerNear = MoneyManager.getMoneyOwner(stackNear);
                if (ownerNear != null && !ownerNear.equalsIgnoreCase(ownerSrc)) continue;

                Skill skillNear = MoneyManager.getMoneyJob(stackNear);
                if (skillNear != skillSrc) continue;

                SkillObjective objectiveNear = MoneyManager.getMoneyObjective(stackNear);
                if (objectiveSrc != objectiveNear) continue;

                Currency currencyNear = MoneyManager.getMoneyCurrency(stackNear);
                if (currencyNear != currencySrc)
                    continue;

                moneySrc += MoneyManager.getMoneyAmount(stackNear);
                near.remove();
            }

            Player player = (ownerSrc != null) ? plugin.getServer().getPlayer(ownerSrc) : null;
            ItemStack money = MoneyManager.createMoney(currencySrc, moneySrc, player, skillSrc, objectiveSrc);
            item.setCustomName(ItemUtil.getItemName(money));
            item.setItemStack(money);
        });
    }
}
