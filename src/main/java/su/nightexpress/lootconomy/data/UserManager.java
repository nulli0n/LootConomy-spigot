package su.nightexpress.lootconomy.data;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserManager;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.LootConomy;

import java.util.UUID;

public class UserManager extends AbstractUserManager<LootConomy, LootUser> {

    public UserManager(@NotNull LootConomy plugin) {
        super(plugin, plugin);
    }

    @Override
    @NotNull
    protected LootUser createData(@NotNull UUID uuid, @NotNull String name) {
        return new LootUser(plugin, uuid, name);
    }
}
