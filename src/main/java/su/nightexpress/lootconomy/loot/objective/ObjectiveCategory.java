package su.nightexpress.lootconomy.loot.objective;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.money.object.MoneyObjective;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.List;
import java.util.function.UnaryOperator;

public class ObjectiveCategory implements Writeable {

//    public static final String DEF_BLOCKS  = "blocks";
//    public static final String DEF_MOBS    = "mobs";
//    public static final String DEF_GATHER  = "gathering";
//    public static final String DEF_SHEAR   = "shearing";
//    public static final String DEF_FISHING = "fishing";

    private final String id;

    private final String       name;
    private final List<String> description;
    private final NightItem    icon;
    //private boolean      isDefault;

    public ObjectiveCategory(@NotNull String id, String name, List<String> description, NightItem icon) {
        this.id = id.toLowerCase();
        this.name = name;
        this.description = description;
        this.icon = icon;
        //this.isDefault = isDefault;
    }

    @NotNull
    public static ObjectiveCategory read(@NotNull FileConfig config, @NotNull String path, @NotNull String id) {
        String name = ConfigValue.create(path + ".Name", StringUtil.capitalizeUnderscored(id)).read(config);
        List<String> description = ConfigValue.create(path + ".Description", Lists.newList()).read(config);
        NightItem icon = ConfigValue.create(path + ".Icon", new NightItem(Material.MAP)).read(config);
        //boolean isDefault = ConfigValue.create(path + ".Default", false).read(config);

        return new ObjectiveCategory(id, name, description, icon);
    }


    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Name", this.name);
        config.set(path + ".Description", this.description);
        config.set(path + ".Icon", this.icon);
        //config.set(path + ".Default", this.isDefault);
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.CATEGORY.replacer(this);
    }

    public boolean isIn(@NotNull MoneyObjective objective) {
        return objective.isCategory(this);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public List<String> getDescription() {
        return this.description;
    }

    @NotNull
    public NightItem getIcon() {
        return this.icon;
    }

//    public boolean isDefault() {
//        return this.isDefault;
//    }
}
