package su.nightexpress.lootconomy.action;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.BukkitThing;

import java.util.function.Function;

public class ObjectFormatters {

    public static <T extends Keyed> ObjectFormatter<T> forKeyed(@NotNull Registry<T> registry, @NotNull Function<T, String> localized) {

        return new ObjectFormatter<T>() {

            @Override
            @NotNull
            public String getName(@NotNull T object) {
                return BukkitThing.toString(object);
            }

            @Override
            @NotNull
            public String getLocalized(@NotNull T object) {
                return localized.apply(object);
            }

            @Override
            @Nullable
            public T parseObject(@NotNull String name) {
                return BukkitThing.fromRegistry(registry, name);
            }
        };
    }

    public static final ObjectFormatter<Material> MATERIAL = forKeyed(Registry.MATERIAL, LangAssets::get);

    public static final ObjectFormatter<EntityType> ENITITY_TYPE = forKeyed(Registry.ENTITY_TYPE, LangAssets::get);

    public static final ObjectFormatter<PotionEffectType> POTION_TYPE = forKeyed(Registry.EFFECT, LangAssets::get);
}
