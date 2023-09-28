package su.nightexpress.lootconomy.config;

import org.bukkit.Sound;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.message.NexParser;

import static su.nexmedia.engine.utils.Colors.*;
import static su.nightexpress.lootconomy.Placeholders.*;

public class Lang extends EngineLang {

    public static final LangKey COMMAND_STATS_DESC    = LangKey.of("Command.Stats.Desc", "Show [player's] skills stats.");
    public static final LangKey COMMAND_STATS_USAGE   = LangKey.of("Command.Stats.Usage", "[player]");
    public static final LangKey COMMAND_STATS_DISPLAY = LangKey.of("Command.Stats.Display",
        "<! prefix:\"false\" !>" +
            "\n" + GRAY +
            "\n" + ORANGE + PLAYER_NAME + "'s Loot Stats:" +
            "\n" + GRAY +
            "\n" + GRAY + "[Hover mouse over elements for details]" +
            "\n" + GRAY +
            "\n" + GRAY + "▪ "
                + "<? show_text:\"" + ORANGE + SKILL_DESCRIPTION + "\" ?>" + ORANGE + "[?]</> "
                + "<? show_text:\"" + GRAY + "Click to for detailed skill info.\" run_command:\"/lootconomy info " + SKILL_ID + "\" ?>" + LIGHT_YELLOW + SKILL_NAME + "</> "
                + "<? show_text:\"" + GRAY + "Level: " + GREEN + SKILL_DATA_LEVEL + NexParser.TAG_NEWLINE + GRAY + "XP: " + GREEN + SKILL_DATA_XP + GRAY + "/" + GREEN + SKILL_DATA_XP_MAX + "\" ?>" + GREEN + "[XP]</> "
                + "<? show_text:\"" + GRAY + "Rank: " + PURPLE + SKILL_DATA_RANK + NexParser.TAG_NEWLINE + GRAY + "Next: " + PURPLE + SKILL_DATA_NEXT_RANK + "\" ?>" + PURPLE + "[Rank]</>" +
            "\n" + GRAY);

    public static final LangKey COMMAND_INFO_USAGE   = LangKey.of("Command.Info.Usage", "<skill>");
    public static final LangKey COMMAND_INFO_DESC    = LangKey.of("Command.Info.Desc", "View skill info.");
    public static final LangKey COMMAND_INFO_DISPLAY = LangKey.of("Command.Info.Display",
        "<! prefix:\"false\" !>" +
            "\n" + GRAY +
            "\n" + YELLOW + "&l" + SKILL_NAME + " Info: " +
            "\n" + GRAY + SKILL_DESCRIPTION +
            "\n" + GRAY +
            "\n" + GREEN + "&lProgress:" +
            "\n" + GREEN + "▪ " + GRAY + "Rank: " + GREEN + SKILL_DATA_RANK + GRAY + " | Next: " + GREEN + SKILL_DATA_NEXT_RANK + GRAY +
            "\n" + GREEN + "▪ " + GRAY + "XP: " + GREEN + SKILL_DATA_XP + GRAY + "/" + GREEN + SKILL_DATA_XP_MAX +
            "\n" + GREEN + "▪ " + GRAY + "Level: " + GREEN + SKILL_DATA_LEVEL + GRAY + "/" + GREEN + SKILL_DATA_LEVEL_MAX +
            "\n" + GRAY +
            "\n" + YELLOW + "&lMultipliers:" +
            "\n" + YELLOW + "▪ " + GRAY + "XP Multiplier: " + YELLOW + "x" + XP_MULTIPLIER +
            "\n" + YELLOW + "▪ " + GRAY + CURRENCY_NAME + " Multiplier: " + YELLOW + "x" + CURRENCY_MULTIPLIER +
            "\n" + GRAY +
            "\n" + PURPLE + "&lBoosters:" +
            "\n" + PURPLE + "▪ " + GRAY + "XP Boost: " + PURPLE + XP_BOOST_PERCENT + "%" +
            "\n" + PURPLE + "▪ " + GRAY + CURRENCY_NAME + " Boost: " + PURPLE + CURRENCY_BOOST_PERCENT + "%" +
            "\n" + PURPLE +
            "\n" + PURPLE + "&lBooster Expiration:" +
            "\n" + PURPLE + "▪ " + GRAY + "Timeleft: " + PURPLE + BOOSTER_TIME_LEFT +
            "\n" + PURPLE);

    public static final LangKey COMMAND_BOOSTER_DESC  = LangKey.of("Command.Booster.Desc", "Booster management.");
    public static final LangKey COMMAND_BOOSTER_USAGE = LangKey.of("Command.Booster.Usage", "");

    public static final LangKey COMMAND_BOOSTER_CREATE_DESC  = LangKey.of("Command.Booster.Create.Desc", "Create personal booster.");
    public static final LangKey COMMAND_BOOSTER_CREATE_USAGE = LangKey.of("Command.Booster.Create.Usage", "<player> <booster> <duration> [-s]");
    public static final LangKey COMMAND_BOOSTER_CREATE_DONE  = LangKey.of("Command.Booster.Create.Done", GRAY + "Added " + GREEN + GENERIC_NAME + GRAY + " booster to " + GREEN + PLAYER_NAME + GRAY + " for " + GREEN + GENERIC_TIME);
    public static final LangKey COMMAND_BOOSTER_CREATE_NOTIFY  = LangKey.of("Command.Booster.Create.Notify",
        "<! prefix:\"false\" sound:\"" + Sound.BLOCK_NOTE_BLOCK_BELL.name() + "\" !>" +
            "\n" + LIGHT_YELLOW +
            "\n" + LIGHT_YELLOW + "You got a Loot Booster:" +
            "\n" + LIGHT_YELLOW + "▪ " + GRAY + "Skills: " + LIGHT_YELLOW + SKILL_NAME +
            "\n" + LIGHT_YELLOW + "▪ " + GRAY + "XP Bonus: " + LIGHT_YELLOW + "+" + XP_BOOST_PERCENT + "%" +
            "\n" + LIGHT_YELLOW + "▪ " + GRAY + CURRENCY_NAME + " Bonus: " + LIGHT_YELLOW + "+" + CURRENCY_BOOST_PERCENT + "%" +
            "\n" + RED + "▪ " + GRAY + "Duration: " + RED + GENERIC_TIME +
            "\n" + LIGHT_YELLOW);

    public static final LangKey COMMAND_BOOSTER_CLEAR_DESC       = LangKey.of("Command.Booster.Clear.Desc", "Remove personal boosters.");
    public static final LangKey COMMAND_BOOSTER_CLEAR_USAGE      = LangKey.of("Command.Booster.Clear.Usage", "<player> [skill]");
    public static final LangKey COMMAND_BOOSTER_CLEAR_DONE_SKILL = LangKey.of("Command.Booster.Clear.Done.Skill", GRAY + "Removed personal " + GREEN + SKILL_NAME + GRAY + " booster from " + GREEN + PLAYER_NAME + GRAY + ".");
    public static final LangKey COMMAND_BOOSTER_CLEAR_DONE_ALL   = LangKey.of("Command.Booster.Clear.Done.All", GRAY + "Removed " + GREEN + "All" + GRAY + " personal boosters from " + GREEN + PLAYER_NAME + GRAY + ".");

    public static final LangKey COMMAND_XP_DESC          = LangKey.of("Command.XP.Desc", "Manage player's skill XP.");
    public static final LangKey COMMAND_XP_USAGE         = LangKey.of("Command.XP.Usage", "<action> <skill> <amount> [player] [-s]");
    public static final LangKey COMMAND_XP_ADD_DONE      = LangKey.of("Command.XP.Add.Done", GRAY + "Added " + GREEN + GENERIC_AMOUNT + GRAY + " XP to " + GREEN + SKILL_NAME + GRAY + " skill for " + GREEN + PLAYER_NAME + GRAY + ".");
    public static final LangKey COMMAND_XP_ADD_NOTIFY    = LangKey.of("Command.XP.Add.Notify", GREEN + GENERIC_AMOUNT + GRAY + " XP has been added to your " + GREEN + SKILL_NAME + GRAY + " skill!");
    public static final LangKey COMMAND_XP_REMOVE_DONE   = LangKey.of("Command.XP.Remove.Done", GRAY + "Removed " + RED + GENERIC_AMOUNT + GRAY + " XP from " + RED + SKILL_NAME + GRAY + " skill of " + RED + PLAYER_NAME + GRAY + ".");
    public static final LangKey COMMAND_XP_REMOVE_NOTIFY = LangKey.of("Command.XP.Remove.Notify", RED + GENERIC_AMOUNT + GRAY + " XP has been removed from your " + RED + SKILL_NAME + GRAY + " skill.");
    public static final LangKey COMMAND_XP_SET_DONE      = LangKey.of("Command.XP.Set.Done", GRAY + "Set " + YELLOW + GENERIC_AMOUNT + GRAY + " XP for " + YELLOW + PLAYER_NAME + GRAY + "'s " + SKILL_NAME + " skill.");
    public static final LangKey COMMAND_XP_SET_NOTIFY    = LangKey.of("Command.XP.Set.Notify", GRAY + "Your " + YELLOW + SKILL_NAME + GRAY + " skill XP has been set to " + YELLOW + GENERIC_AMOUNT + GRAY + ".");

    public static final LangKey COMMAND_LEVEL_DESC          = LangKey.of("Command.Level.Desc", "Manage player's skill levels.");
    public static final LangKey COMMAND_LEVEL_USAGE         = LangKey.of("Command.Level.Usage", "<action> <skill> <amount> [player] [-s]");
    public static final LangKey COMMAND_LEVEL_ADD_DONE      = LangKey.of("Command.Level.Add.Done", GRAY + "Added " + GREEN + GENERIC_AMOUNT + GRAY + " level(s) to " + GREEN + SKILL_NAME + GRAY + " skill for " + GREEN + PLAYER_NAME + GRAY + ".");
    public static final LangKey COMMAND_LEVEL_ADD_NOTIFY    = LangKey.of("Command.Level.Add.Notify", GREEN + GENERIC_AMOUNT + GRAY + " level(s) has been added to your " + GREEN + SKILL_NAME + GRAY + " skill!");
    public static final LangKey COMMAND_LEVEL_REMOVE_DONE   = LangKey.of("Command.Level.Remove.Done", GRAY + "Removed " + RED + GENERIC_AMOUNT + GRAY + " level(s) from " + RED + SKILL_NAME + GRAY + " skill of " + RED + PLAYER_NAME + GRAY + ".");
    public static final LangKey COMMAND_LEVEL_REMOVE_NOTIFY = LangKey.of("Command.Level.Remove.Notify", RED + GENERIC_AMOUNT + GRAY + " level(s) has been removed from your " + RED + SKILL_NAME + GRAY + " skill.");
    public static final LangKey COMMAND_LEVEL_SET_DONE      = LangKey.of("Command.Level.Set.Done", GRAY + "Set " + YELLOW + GENERIC_AMOUNT + GRAY + " level for " + YELLOW + PLAYER_NAME + GRAY + "'s " + SKILL_NAME + " skill.");
    public static final LangKey COMMAND_LEVEL_SET_NOTIFY    = LangKey.of("Command.Level.Set.Notify", GRAY + "Your " + YELLOW + SKILL_NAME + GRAY + " skill level has been set to " + YELLOW + GENERIC_AMOUNT + GRAY + ".");

    public static final LangKey COMMAND_RESET_DESC  = LangKey.of("Command.Reset.Desc", "Reset [player's] skill progress.");
    public static final LangKey COMMAND_RESET_USAGE = LangKey.of("Command.Reset.Usage", "<skill> [player] [-s]");
    public static final LangKey COMMAND_RESET_DONE  = LangKey.of("Command.Reset.Done", GRAY + "Successfully reset " + GREEN + SKILL_NAME + GRAY + " progress for " + GREEN + PLAYER_NAME + GRAY + ".");

    public static final LangKey COMMAND_SKILLS_DESC  = LangKey.of("Command.Skills.Desc", "Open skills menu.");
    public static final LangKey COMMAND_SKILLS_USAGE = LangKey.of("Command.Skills.Usage", "");

    public static final LangKey COMMAND_OBJECTIVES_USAGE = LangKey.of("Command.Objectives.Usage", "<skill>");
    public static final LangKey COMMAND_OBJECTIVES_DESC  = LangKey.of("Command.Objectives.Desc", "View skill objectives.");

    public static final LangKey COMMAND_DROP_USAGE = LangKey.of("Command.Drop.Usage", "<currency> <min> <max> <world> <x> <y> <z>");
    public static final LangKey COMMAND_DROP_DESC  = LangKey.of("Command.Drop.Desc", "Create and drop currency item.");
    public static final LangKey COMMAND_DROP_DONE  = LangKey.of("Command.Drop.Done", GRAY + "Dropped " + YELLOW + GENERIC_AMOUNT + GRAY + " currency item at " + YELLOW + LOCATION_X + ", " + LOCATION_Y + ", " + LOCATION_Z + GRAY + " in " + YELLOW + LOCATION_WORLD + GRAY + ".");

    public static final LangKey COMMAND_SOUND_DESC = LangKey.of("Command.Sound.Desc", "Switch on/off money pickup sound.");
    public static final LangKey COMMAND_SOUND_DONE = LangKey.of("Command.Sound.Done",
        "<! sound:\"" + Sound.UI_BUTTON_CLICK.name() + "\" !>" + GRAY + "Money pickup sound: " + YELLOW + GENERIC_STATE);

    public static final LangKey COMMAND_TOP_USAGE = LangKey.of("Command.Currency.Top.Usage", "<skill> [page]");
    public static final LangKey COMMAND_TOP_DESC = LangKey.of("Command.Currency.Top.Desc", "List of players with the most level.");
    public static final LangKey COMMAND_TOP_LIST = LangKey.of("Command.Currency.Top.List",
        "<! prefix:\"false\" !>" +
            "\n" + CYAN +
            "\n" + CYAN + "&l" + SKILL_NAME + " Level Top:" +
            "\n" + CYAN +
            "\n" + CYAN + GENERIC_POS + ". " + GRAY + PLAYER_NAME + ": " + CYAN + GENERIC_AMOUNT + GRAY + " Levels" +
            "\n" + CYAN +
            "\n" + GRAY + "Page " + CYAN + GENERIC_CURRENT + GRAY + " of " + CYAN + GENERIC_MAX + GRAY + "." +
            "\n" + CYAN);

    public static final LangKey BOOSTER_ERROR_INVALID = LangKey.of("Booster.Error.Invalid", RED + "Invalid booster!");

    public static final LangKey CURRENCY_ERROR_INVALID = LangKey.of("Currency.Error.Invalid", RED + "Invalid currency!");

    public static final LangKey SKILL_ERROR_INVALID = LangKey.of("Skill.Error.Invalid", RED + "Invalid skill!");

    public static final LangKey SKILL_RESET_NOTIFY = LangKey.of("Skill.Reset.Notify",
        "<! type:\"titles:20:60:20\" sound:\"" + Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR.name() + "\" !>" +
            "\n" + RED + "&lSkill Reset!" +
            "\n" + GRAY + "All " + RED + SKILL_NAME + GRAY + " progress have been " + RED + "reset" + GRAY + "!");


    public static final LangKey SKILL_XP_GAIN = LangKey.of("Skill.XP.Gain",
        "<! type:\"action_bar\" !>" +
            YELLOW + "&l" + SKILL_NAME + ": " + LIGHT_YELLOW + "+" + GENERIC_AMOUNT + " XP.");

    public static final LangKey SKILL_XP_LOSE = LangKey.of("Skill.XP.Lose", GRAY + "You lost " + RED + GENERIC_AMOUNT + " XP" + GRAY + " from " + RED + SKILL_NAME + GRAY + " skill.");

    public static final LangKey SKILL_LEVEL_UP = LangKey.of("Skill.Level.Up",
        "<! type:\"titles:20:60:20\" sound:\"" + Sound.ENTITY_PLAYER_LEVELUP.name() + "\" !>" +
            "\n" + GREEN + "&lSkill Level Up!" +
            "\n" + GREEN + SKILL_NAME + GRAY + " is now level " + GREEN + SKILL_DATA_LEVEL + GRAY + "!");

    public static final LangKey SKILL_LEVEL_DOWN = LangKey.of("Skill.Level.Down",
        "<! type:\"titles:20:60:20\" sound:\"" + Sound.ENTITY_IRON_GOLEM_DEATH.name() + "\" !>" +
            "\n" + RED + "&lSkill Level Down!" +
            "\n" + RED + SKILL_NAME + GRAY + " is now level " + RED + SKILL_DATA_LEVEL + GRAY + "!");

    public static final LangKey SKILL_LIMIT_XP_NOTIFY = LangKey.of("Skill.Limit.XP.Notify",
        GRAY + "You have reached daily XP limit for " + SKILL_NAME + GRAY + " skill. You can't get more today.");

    public static final LangKey SKILL_LIMIT_CURRENCY_NOTIFY = LangKey.of("Skill.Limit.Currency.Notify",
        GRAY + "You have reached daily " + RED + CURRENCY_NAME + GRAY + " limit for " + SKILL_NAME + GRAY + " skill. You can't get more today.");

    public static final LangKey CURRENCY_PICKUP = LangKey.of("Currency.Pickup",
        "<! type:\"action_bar\" !>" +
            GRAY + "You picked up " + GREEN + GENERIC_AMOUNT + GRAY + "! Balance: " + GREEN + GENERIC_BALANCE);

    public static final LangKey CURRENCY_LOST = LangKey.of("Currency.Lost",
        "<! type:\"action_bar\" !>" +
            GRAY + "You lost " + RED + GENERIC_AMOUNT + GRAY + "! Balance: " + RED + GENERIC_AMOUNT);
}
