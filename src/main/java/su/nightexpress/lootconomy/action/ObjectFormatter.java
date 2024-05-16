package su.nightexpress.lootconomy.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ObjectFormatter<O> {

    @NotNull
    String getName(@NotNull O object);

    @NotNull
    String getLocalized(@NotNull O object);

    @Nullable
    O parseObject(@NotNull String name);

    @NotNull
    default String getLocalized(@NotNull String name) {
        O object = this.parseObject(name);
        return object == null ? name : this.getLocalized(object);
    }
}
