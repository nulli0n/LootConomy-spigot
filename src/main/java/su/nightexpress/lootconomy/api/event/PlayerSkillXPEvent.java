package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.data.impl.LootUser;

public abstract class PlayerSkillXPEvent extends PlayerSkillEvent implements Cancellable {

    protected boolean isCancelled;
    protected String source;
    protected int    xp;

    public PlayerSkillXPEvent(@NotNull Player player, @NotNull LootUser user, @NotNull SkillData skillData,
                              @NotNull String source, int xp) {
        super(player, user, skillData);
        this.setXP(xp);
        this.setSource(source);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled || this.getXP() == 0;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @NotNull
    public final String getSource() {
        return source;
    }

    public final void setSource(@NotNull String source) {
        this.source = source;
    }

    public final int getXP() {
        return xp;
    }

    public final void setXP(int xp) {
        this.xp = Math.abs(xp);
    }
}
