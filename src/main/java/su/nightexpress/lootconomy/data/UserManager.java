package su.nightexpress.lootconomy.data;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.data.impl.LootUser;
import su.nightexpress.lootconomy.LootConomyPlugin;
import su.nightexpress.nightcore.db.AbstractUserManager;

import java.util.UUID;

public class UserManager extends AbstractUserManager<LootConomyPlugin, LootUser> {

    public UserManager(@NotNull LootConomyPlugin plugin, @NotNull DataHandler dataHandler) {
        super(plugin, dataHandler);
    }

    @Override
    @NotNull
    public LootUser create(@NotNull UUID uuid, @NotNull String name) {
        return LootUser.create(uuid, name);
    }
}
