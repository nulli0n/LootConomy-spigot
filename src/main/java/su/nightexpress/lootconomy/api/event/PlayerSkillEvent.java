package su.nightexpress.lootconomy.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.skill.impl.Skill;

public abstract class PlayerSkillEvent extends Event {

    protected Player    player;
    protected LootUser  user;
    protected SkillData skillData;

    public PlayerSkillEvent(@NotNull Player player, @NotNull LootUser user, @NotNull SkillData skillData) {
        this.player = player;
        this.user = user;
        this.skillData = skillData;
    }

    @NotNull
    public final Player getPlayer() {
        return this.player;
    }

    @NotNull
    public final SkillData getSkillData() {
        return this.skillData;
    }

    @NotNull
    public final Skill getSkill() {
        return this.skillData.getSkill();
    }

    @NotNull
    public final LootUser getUser() {
        return user;
    }
}
