package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.data.impl.LootUser;

public class PlayerSkillXPLoseEvent extends PlayerSkillXPEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerSkillXPLoseEvent(@NotNull Player player, @NotNull LootUser user, @NotNull SkillData skillData,
                                  @NotNull String source, int exp) {
        super(player, user, skillData, source, Math.abs(exp));
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
