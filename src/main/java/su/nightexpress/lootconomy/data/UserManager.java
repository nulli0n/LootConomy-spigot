package su.nightexpress.lootconomy.data;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.nightcore.database.AbstractUserManager;

import java.util.UUID;

public class UserManager extends AbstractUserManager<LootConomyPlugin, LootUser> {

    public UserManager(@NotNull LootConomyPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public LootUser createUserData(@NotNull UUID uuid, @NotNull String name) {
        return LootUser.create(this.plugin, uuid, name);
    }
}
