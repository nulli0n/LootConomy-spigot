package su.nightexpress.lootconomy.config;

import org.bukkit.Sound;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;
import su.nightexpress.nightcore.language.message.OutputType;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.lootconomy.Placeholders.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.*;

public class Lang extends CoreLang {

    public static final LangString COMMAND_ARGUMENT_NAME_CURRENCY   = LangString.of("Command.Argument.Name.Currency", "currency");
    public static final LangString COMMAND_ARGUMENT_NAME_MULTIPLIER = LangString.of("Command.Argument.Name.Multiplier", "multiplier");
    public static final LangString COMMAND_ARGUMENT_NAME_DURATION   = LangString.of("Command.Argument.Name.Duration", "duration");
    public static final LangString COMMAND_ARGUMENT_NAME_MIN        = LangString.of("Command.Argument.Name.Min", "min");
    public static final LangString COMMAND_ARGUMENT_NAME_MAX        = LangString.of("Command.Argument.Name.Max", "max");
    public static final LangString COMMAND_ARGUMENT_NAME_COUNT      = LangString.of("Command.Argument.Name.Count", "count");
    public static final LangString COMMAND_ARGUMENT_NAME_X          = LangString.of("Command.Argument.Name.X", "x");
    public static final LangString COMMAND_ARGUMENT_NAME_Y          = LangString.of("Command.Argument.Name.Y", "y");
    public static final LangString COMMAND_ARGUMENT_NAME_Z          = LangString.of("Command.Argument.Name.Z", "z");

    public static final LangString COMMAND_BOOSTS_DESC = LangString.of("Command.Boosters.Desc",
        "View all current boosters.");

    public static final LangString COMMAND_BOOSTER_DESC = LangString.of("Command.Booster.Desc",
        "Booster management.");

    public static final LangString COMMAND_BOOSTER_ACTIVATE_DESC = LangString.of("Command.Booster.Activate.Desc",
        "Activate global scheduled booster.");

    public static final LangText COMMAND_BOOSTER_ACTIVATE_DONE = LangText.of("Command.Booster.Activate.Done",
        LIGHT_GRAY.enclose("Global scheduled booster activated!")
    );

    public static final LangString COMMAND_BOOSTER_CREATE_DESC = LangString.of("Command.Booster.Create.Desc",
        "Create global or player booster.");

    public static final LangText COMMAND_BOOSTER_CREATE_DONE_GLOBAL = LangText.of("Command.Booster.Create.Done.Global",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Global Booster Created:")),
        LIGHT_YELLOW.enclose("● " + LIGHT_GRAY.enclose("ID: ") + GENERIC_NAME),
        LIGHT_YELLOW.enclose("● " + LIGHT_GRAY.enclose("Multiplier: ") + CURRENCY_NAME + " x" + GENERIC_MULTIPLIER),
        LIGHT_YELLOW.enclose("● " + LIGHT_GRAY.enclose("Duration: ") + GENERIC_DURATION),
        " "
    );

    public static final LangText COMMAND_BOOSTER_CREATE_DONE_PLAYER = LangText.of("Command.Booster.Create.Done.Player",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Player Booster Created:")),
        LIGHT_YELLOW.enclose("● " + LIGHT_GRAY.enclose("ID: ") + GENERIC_NAME),
        LIGHT_YELLOW.enclose("● " + LIGHT_GRAY.enclose("Target: ") + PLAYER_NAME),
        LIGHT_YELLOW.enclose("● " + LIGHT_GRAY.enclose("Multiplier: ") + CURRENCY_NAME + " x" + GENERIC_MULTIPLIER),
        LIGHT_YELLOW.enclose("● " + LIGHT_GRAY.enclose("Duration: ") + GENERIC_DURATION),
        " "
    );


    public static final LangString COMMAND_BOOSTER_INFO_DESC = LangString.of("Command.Booster.Info.Desc",
        "View active booster names.");

    public static final LangText COMMAND_BOOSTER_INFO_NOTHING = LangText.of("Command.Booster.Info.Nothing",
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " There are no active boosters.")
    );

    public static final LangText COMMAND_BOOSTER_INFO_LIST = LangText.of("Command.Booster.Info.List",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Booster List:")),
        GENERIC_ENTRY,
        " "
    );

    public static final LangString COMMAND_BOOSTER_INFO_ENTRY = LangString.of("Command.Booster.Info.Entry",
        LIGHT_YELLOW.enclose("● " + LIGHT_GRAY.enclose(GENERIC_NAME + ": ") + GENERIC_CURRENCY)
    );

    public static final LangString COMMAND_BOOSTER_INFO_CURRENCY = LangString.of("Command.Booster.Info.Currency",
        CURRENCY_NAME + " x" + GENERIC_AMOUNT
    );

    public static final LangString COMMAND_BOOSTER_REMOVE_DESC_GLOBAL = LangString.of("Command.Booster.Remove.Desc.Global",
        "Remove global booster.");

    public static final LangString COMMAND_BOOSTER_REMOVE_DESC_PLAYER = LangString.of("Command.Booster.Remove.Desc.Player",
        "Remove player booster.");

    public static final LangText COMMAND_BOOSTER_REMOVE_DONE_PLAYER = LangText.of("Command.Booster.Remove.Done.Player",
        LIGHT_GRAY.enclose("Removed " + LIGHT_YELLOW.enclose(GENERIC_NAME) + " booster from " + LIGHT_YELLOW.enclose(PLAYER_NAME) + ".")
    );

    public static final LangText COMMAND_BOOSTER_REMOVE_DONE_GLOBAL = LangText.of("Command.Booster.Remove.Done.Global",
        LIGHT_GRAY.enclose("Removed " + LIGHT_YELLOW.enclose(GENERIC_NAME) + " global server booster.")
    );

    public static final LangText COMMAND_BOOSTER_REMOVE_ERROR_NOTHING = LangText.of("Command.Booster.Remove.Error.Nothing",
        LIGHT_GRAY.enclose(LIGHT_RED.enclose("✘") + " There is no booster with such name.")
    );


    public static final LangString COMMAND_OBJECTIVES_DESC = LangString.of("Command.Objectives.Desc",
        "View money objectives.");

    public static final LangText COMMAND_OBJECTIVES_DONE_OTHERS = LangText.of("Command.Objectives.Done.Others",
        LIGHT_GRAY.enclose("Opened objectives menu for " + LIGHT_YELLOW.enclose(PLAYER_NAME) + "."));


    public static final LangString COMMAND_DROP_DESC = LangString.of("Command.Drop.Desc",
        "Create and drop currency item.");

    public static final LangText COMMAND_DROP_DONE = LangText.of("Command.Drop.Done",
        LIGHT_GRAY.enclose("Dropped " + LIGHT_YELLOW.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_MIN + " - " + GENERIC_MAX) + " item(s) at " + LIGHT_YELLOW.enclose(LOCATION_X) + ", " + LIGHT_YELLOW.enclose(LOCATION_Y) + ", " + LIGHT_YELLOW.enclose(LOCATION_Z) + " in " + LIGHT_YELLOW.enclose(LOCATION_WORLD) + ".")
    );


    public static final LangString COMMAND_SOUND_DESC = LangString.of("Command.Sound.Desc",
        "Toggle money pickup sound.");

    public static final LangText COMMAND_SOUND_DONE = LangText.of("Command.Sound.Done",
        SOUND.enclose(Sound.UI_BUTTON_CLICK),
        LIGHT_GRAY.enclose("Money pickup sound: " + LIGHT_YELLOW.enclose(GENERIC_STATE))
    );


    public static final LangText ERROR_INVALID_BOOSTER = LangText.of("Error.InvalidBooster",
        LIGHT_RED.enclose("Invalid booster!"));

    public static final LangText ERROR_INVALID_CURRENCY = LangText.of("Error.InvalidCurrency",
        LIGHT_RED.enclose("Invalid currency!"));


    public static final LangText CURRENCY_LIMIT_NOTIFY = LangText.of("Currency.Limit.Notify",
        TAG_NO_PREFIX,
        " ",
        LIGHT_RED.enclose(BOLD.enclose("Limit Notification:")),
        LIGHT_GRAY.enclose("You reached daily " + LIGHT_RED.enclose(GENERIC_MAX) + " limit for " + LIGHT_RED.enclose(CURRENCY_NAME) + "."),
        LIGHT_GRAY.enclose("You can't get more " + LIGHT_RED.enclose(CURRENCY_NAME) + " in loot until next day."),
        " "
    );

    public static final LangText CURRENCY_PICKUP = LangText.of("Currency.Pickup",
        OUTPUT.enclose(OutputType.ACTION_BAR),
        LIGHT_GRAY.enclose("You picked up " + LIGHT_GREEN.enclose(GENERIC_AMOUNT) + "! Balance: " + LIGHT_GREEN.enclose(GENERIC_BALANCE))
    );

    public static final LangText CURRENCY_LOST_PENALTY = LangText.of("Currency.Lost.Penalty",
        TAG_NO_PREFIX,
        LIGHT_GRAY.enclose("You paid " + LIGHT_RED.enclose(GENERIC_AMOUNT) + " penalty for " + LIGHT_RED.enclose(GENERIC_NAME) + "!")
    );

    public static final LangText CURRENCY_LOST_DEATH = LangText.of("Currency.Lost.Death",
        TAG_NO_PREFIX,
        LIGHT_GRAY.enclose("You lost " + LIGHT_RED.enclose(GENERIC_AMOUNT) + " on death!"),
        LIGHT_GRAY.enclose("New balance: " + LIGHT_RED.enclose(GENERIC_BALANCE))
    );


    public static final LangText BOOSTER_NOTIFY_SCHEDULED = LangText.of("Booster.Notify.Scheduled",
        TAG_NO_PREFIX + SOUND.enclose(Sound.BLOCK_NOTE_BLOCK_BELL),
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Booster Activated!")),
        LIGHT_GRAY.enclose("A regular scheduled booster have been " + LIGHT_YELLOW.enclose("activated") + "!"),
        LIGHT_GRAY.enclose("The following multipliers were applied:"),
        " ",
        GENERIC_ENTRY,
        " ",
        LIGHT_RED.enclose("[❗] " + LIGHT_GRAY.enclose("Boost duration: ") + GENERIC_TIME),
        " "
    );

    public static final LangText BOOSTER_NOTIFY_CUSTOM = LangText.of("Booster.Notify.Custom",
        TAG_NO_PREFIX + SOUND.enclose(Sound.BLOCK_NOTE_BLOCK_BELL),
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Booster Activated!")),
        LIGHT_GRAY.enclose("A new booster just have been created!"),
        LIGHT_GRAY.enclose("The following multipliers were applied:"),
        " ",
        GENERIC_ENTRY,
        " ",
        LIGHT_RED.enclose("[❗] " + LIGHT_GRAY.enclose("Boost duration: ") + GENERIC_TIME),
        " "
    );

    public static final LangString BOOSTER_NOTIFY_ENTRY = LangString.of("Booster.Notify.Entry",
        LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose(CURRENCY_NAME + ": ") + GENERIC_AMOUNT)
    );


    public static final LangText BOOSTER_LIST_INFO = LangText.of("Booster.List.Info",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Booster Info:")),
        LIGHT_GRAY.enclose("Here is a list of all currently " + LIGHT_YELLOW.enclose("active boosters") + "."),
        LIGHT_GRAY.enclose("Hover over currency percent amount for " + LIGHT_YELLOW.enclose("details") + "!"),
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Global:")),
        GENERIC_GLOBAL,
        " ",
        LIGHT_YELLOW.enclose(BOLD.enclose("Personal:")),
        GENERIC_PERSONAL,
        " "
    );

    public static final LangString BOOSTER_LIST_ENTRY_CURRENCY = LangString.of("Booster.List.Entry.Currency",
        LIGHT_YELLOW.enclose("✔ " + LIGHT_GRAY.enclose(CURRENCY_NAME + ": ") +
            HOVER.enclose(GENERIC_TOTAL, LIGHT_GRAY.enclose("Including:") + TAG_LINE_BREAK + GENERIC_ENTRY)
        )
    );

    public static final LangString BOOSTER_LIST_ENTRY_HOVER = LangString.of("Booster.List.Entry.CurrencyHover",
        LIGHT_YELLOW.enclose(GENERIC_AMOUNT) + " " + GRAY.enclose("(" + WHITE.enclose(GENERIC_TIME) + ")")
    );

    public static final LangString BOOSTER_LIST_GLOBAL_NOTHING = LangString.of("Booster.List.Global.Nothing",
        LIGHT_RED.enclose("There is no active global boosters.")
    );

    public static final LangString BOOSTER_LIST_PERSONAL_NOTHING = LangString.of("Booster.List.Personal.Nothing",
        LIGHT_RED.enclose("You have no active boosters.")
    );
}
