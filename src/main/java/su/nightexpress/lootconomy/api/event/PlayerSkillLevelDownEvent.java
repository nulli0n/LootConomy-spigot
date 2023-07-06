package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.data.impl.LootUser;

public class PlayerSkillLevelDownEvent extends PlayerSkillEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerSkillLevelDownEvent(@NotNull Player player, @NotNull LootUser user, @NotNull SkillData data) {
        super(player, user, data);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public int getNewLevel() {
        return this.getSkillData().getLevel();
    }
}
