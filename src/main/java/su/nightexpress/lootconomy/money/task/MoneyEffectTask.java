package su.nightexpress.lootconomy.money.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.money.MoneyManager;

public class MoneyEffectTask extends AbstractTask<LootConomy> {

    private final MoneyManager moneyManager;

    public MoneyEffectTask(@NotNull MoneyManager moneyManager) {
        super(moneyManager.plugin(), 5L, true);
        this.moneyManager = moneyManager;
    }

    @Override
    public void action() {
        this.moneyManager.getTrackedLoot().forEach(item -> {
            Currency currency = MoneyManager.getMoneyCurrency(item.getItemStack());
            if (currency == null) return;

            SimpleParticle particle = currency.getGroundEffect();
            if (particle == null) return;

            particle.play(item.getLocation(), 0.15, 0.15, 2);
        });
    }
}
