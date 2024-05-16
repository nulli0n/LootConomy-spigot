package su.nightexpress.lootconomy.config;

import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class Perms {

    public static final String PREFIX                      = "lootconomy.";
    public static final String PREFIX_COMMAND              = PREFIX + "command.";
    public static final String PREFIX_BYPASS               = PREFIX + "bypass.";
    public static final String PREFIX_BYPASS_DAILY_LIMIT   = PREFIX_BYPASS + "dailylimit.";
    public static final String PREFIX_BYPASS_DEATH_PENALTY = PREFIX_BYPASS + "death.penalty.";

    public static final UniPermission PLUGIN  = new UniPermission(PREFIX + Placeholders.WILDCARD);
    public static final UniPermission COMMAND = new UniPermission(PREFIX_COMMAND + Placeholders.WILDCARD);
    public static final UniPermission BYPASS  = new UniPermission(PREFIX_BYPASS + Placeholders.WILDCARD);

    public static final UniPermission COMMAND_BOOSTER           = new UniPermission(PREFIX_COMMAND + "booster");
    public static final UniPermission COMMAND_BOOSTS            = new UniPermission(PREFIX_COMMAND + "boosts");
    public static final UniPermission COMMAND_DROP              = new UniPermission(PREFIX_COMMAND + "drop");
    public static final UniPermission COMMAND_OBJECTIVES        = new UniPermission(PREFIX_COMMAND + "objectives");
    public static final UniPermission COMMAND_OBJECTIVES_OTHERS = new UniPermission(PREFIX_COMMAND + "objectives.others");
    public static final UniPermission COMMAND_SOUND             = new UniPermission(PREFIX_COMMAND + "sound");
    //public static final UniPermission COMMAND_TOP               = new UniPermission(PREFIX_COMMAND + "top");
    public static final UniPermission COMMAND_RELOAD            = new UniPermission(PREFIX_COMMAND + "reload");

    public static final UniPermission BYPASS_DEATH_PENALTY = new UniPermission(PREFIX_BYPASS_DEATH_PENALTY + Placeholders.WILDCARD);
    public static final UniPermission BYPASS_DAILY_LIMIT   = new UniPermission(PREFIX_BYPASS_DAILY_LIMIT + Placeholders.WILDCARD);

    static {
        PLUGIN.addChildren(COMMAND, BYPASS);

        COMMAND.addChildren(
            COMMAND_RELOAD,
            COMMAND_OBJECTIVES, COMMAND_OBJECTIVES_OTHERS,
            COMMAND_DROP,
            COMMAND_SOUND,
            COMMAND_BOOSTER,
            COMMAND_BOOSTS
        );

        BYPASS.addChildren(
            BYPASS_DEATH_PENALTY,
            BYPASS_DAILY_LIMIT
        );
    }
}
