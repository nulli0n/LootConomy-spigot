package su.nightexpress.lootconomy.config;

import su.nexmedia.engine.api.server.JPermission;
import su.nightexpress.lootconomy.Placeholders;

public class Perms {

    private static final String PREFIX = "lootconomy.";
    private static final String PREFIX_COMMAND = PREFIX + "command.";
    private static final String PREFIX_BYPASS = PREFIX + "bypass.";
    public static final String PREFIX_SKILL = PREFIX + "skill.";

    public static final JPermission PLUGIN  = new JPermission(PREFIX + Placeholders.WILDCARD);
    public static final JPermission COMMAND = new JPermission(PREFIX_COMMAND + Placeholders.WILDCARD);
    public static final JPermission BYPASS  = new JPermission(PREFIX_BYPASS + Placeholders.WILDCARD);
    public static final JPermission SKILL   = new JPermission(PREFIX_SKILL + Placeholders.WILDCARD);

    public static final JPermission COMMAND_BOOSTER      = new JPermission(PREFIX_COMMAND + "booster");
    public static final JPermission COMMAND_DROP         = new JPermission(PREFIX_COMMAND + "drop");
    public static final JPermission COMMAND_STATS        = new JPermission(PREFIX_COMMAND + "stats");
    public static final JPermission COMMAND_INFO         = new JPermission(PREFIX_COMMAND + "info");
    public static final JPermission COMMAND_SKILLS       = new JPermission(PREFIX_COMMAND + "skills");
    public static final JPermission COMMAND_XP           = new JPermission(PREFIX_COMMAND + "xp");
    public static final JPermission COMMAND_LEVEL        = new JPermission(PREFIX_COMMAND + "level");
    public static final JPermission COMMAND_OBJECTIVES   = new JPermission(PREFIX_COMMAND + "objectives");
    public static final JPermission COMMAND_SOUND = new JPermission(PREFIX_COMMAND + "sound");
    public static final JPermission COMMAND_RESET        = new JPermission(PREFIX_COMMAND + "reset");
    public static final JPermission COMMAND_RESET_OTHERS = new JPermission(PREFIX_COMMAND + "reset.others");
    public static final JPermission COMMAND_TOP          = new JPermission(PREFIX_COMMAND + "top");
    public static final JPermission COMMAND_RELOAD       = new JPermission(PREFIX_COMMAND + "reload");

    public static final JPermission BYPASS_DEATH_PENALTY          = new JPermission(PREFIX_BYPASS + "death.penalty");
    public static final JPermission BYPASS_OBJECTIVE_UNLOCK_LEVEL = new JPermission(PREFIX_BYPASS + "objective.unlock.level");
    public static final JPermission BYPASS_SKILL_LIMIT_XP         = new JPermission(PREFIX_BYPASS + "skill.limit.xp");
    public static final JPermission BYPASS_SKILL_LIMIT_CURRENCY   = new JPermission(PREFIX_BYPASS + "skill.limit.currency");

    static {
        PLUGIN.addChildren(COMMAND, BYPASS, SKILL);

        COMMAND.addChildren(
            COMMAND_RELOAD,
            COMMAND_INFO, COMMAND_STATS, COMMAND_SKILLS, COMMAND_OBJECTIVES,
            COMMAND_LEVEL, COMMAND_XP, COMMAND_DROP,
            COMMAND_RESET, COMMAND_RESET_OTHERS, COMMAND_SOUND,
            COMMAND_BOOSTER
        );

        BYPASS.addChildren(
            BYPASS_DEATH_PENALTY,
            BYPASS_SKILL_LIMIT_CURRENCY, BYPASS_SKILL_LIMIT_XP,
            BYPASS_OBJECTIVE_UNLOCK_LEVEL
        );
    }
}
