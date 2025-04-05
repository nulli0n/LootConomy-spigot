package su.nightexpress.lootconomy.config;

import org.bukkit.Sound;
import su.nightexpress.lootconomy.booster.BoosterType;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;
import su.nightexpress.nightcore.language.message.OutputType;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.lootconomy.Placeholders.*;
import static su.nightexpress.nightcore.language.tag.MessageTags.*;

public class Lang extends CoreLang {

    public static final LangEnum<BoosterType> BOOSTER_TYPE = LangEnum.of("BoosterType", BoosterType.class);

    public static final LangString COMMAND_ARGUMENT_NAME_CURRENCY   = LangString.of("Command.Argument.Name.Currency", "currency");
    public static final LangString COMMAND_ARGUMENT_NAME_MULTIPLIER = LangString.of("Command.Argument.Name.Multiplier", "multiplier");
    public static final LangString COMMAND_ARGUMENT_NAME_DURATION   = LangString.of("Command.Argument.Name.Duration", "duration");
    public static final LangString COMMAND_ARGUMENT_NAME_MIN        = LangString.of("Command.Argument.Name.Min", "min");
    public static final LangString COMMAND_ARGUMENT_NAME_MAX        = LangString.of("Command.Argument.Name.Max", "max");
    public static final LangString COMMAND_ARGUMENT_NAME_COUNT      = LangString.of("Command.Argument.Name.Count", "count");
    public static final LangString COMMAND_ARGUMENT_NAME_X          = LangString.of("Command.Argument.Name.X", "x");
    public static final LangString COMMAND_ARGUMENT_NAME_Y          = LangString.of("Command.Argument.Name.Y", "y");
    public static final LangString COMMAND_ARGUMENT_NAME_Z          = LangString.of("Command.Argument.Name.Z", "z");

    public static final LangString COMMAND_BOOSTS_DESC           = LangString.of("Command.Boosters.Desc", "View all current boosters.");
    public static final LangString COMMAND_BOOSTER_DESC          = LangString.of("Command.Booster.Desc", "Booster management.");
    public static final LangString COMMAND_BOOSTER_ACTIVATE_DESC = LangString.of("Command.Booster.Activate.Desc", "Activate global scheduled booster.");
    public static final LangString COMMAND_BOOSTER_CREATE_DESC   = LangString.of("Command.Booster.Create.Desc", "Create global or player booster.");
    public static final LangString COMMAND_BOOSTER_INFO_DESC     = LangString.of("Command.Booster.Info.Desc", "View active booster names.");
    public static final LangString COMMAND_BOOSTER_REMOVE_DESC   = LangString.of("Command.Booster.Removal.Desc", "Remove global or personal booster.");

    public static final LangString COMMAND_OBJECTIVES_DESC = LangString.of("Command.Objectives.Desc", "View money objectives.");
    public static final LangString COMMAND_DROP_DESC       = LangString.of("Command.Drop.Desc", "Create and drop currency item.");
    public static final LangString COMMAND_SOUND_DESC      = LangString.of("Command.Sound.Desc", "Toggle money pickup sound.");

    public static final LangText COMMAND_BOOSTER_ACTIVATE_DONE = LangText.of("Command.Booster.Activate.Done",
        LIGHT_GRAY.wrap("Booster activated!")
    );

    public static final LangText COMMAND_BOOSTER_CREATE_DONE_GLOBAL = LangText.of("Command.Booster.Create.Done.Global",
        LIGHT_GRAY.wrap("Added global " + LIGHT_YELLOW.wrap(GENERIC_AMOUNT) + " currency booster " + GRAY.wrap("(" + WHITE.wrap(GENERIC_TIME) + ")"))
    );

    public static final LangText COMMAND_BOOSTER_CREATE_DONE_PERSONAL = LangText.of("Command.Booster.Create.Done.Personal",
        LIGHT_GRAY.wrap("Added personal " + LIGHT_YELLOW.wrap(GENERIC_AMOUNT) + " currency booster " + GRAY.wrap("(" + WHITE.wrap(GENERIC_TIME) + ")") + " for " + LIGHT_YELLOW.wrap(PLAYER_NAME))
    );

    public static final LangText COMMAND_BOOSTER_REMOVE_DONE_PERSONAL = LangText.of("Command.Booster.Remove.Done.Personal",
        LIGHT_GRAY.wrap("Disabled " + LIGHT_YELLOW.wrap(PLAYER_NAME) + "'s currency booster.")
    );

    public static final LangText COMMAND_BOOSTER_REMOVE_DONE_GLOBAL = LangText.of("Command.Booster.Remove.Done.Global",
        LIGHT_GRAY.wrap("Disabled global currency booster.")
    );

    public static final LangText COMMAND_BOOSTER_REMOVE_ERROR_NOTHING = LangText.of("Command.Booster.Remove.Error.Nothing",
        LIGHT_RED.wrap("There is no booster.")
    );



    public static final LangText COMMAND_OBJECTIVES_DONE_OTHERS = LangText.of("Command.Objectives.Done.Others",
        LIGHT_GRAY.wrap("Opened objectives menu for " + LIGHT_YELLOW.wrap(PLAYER_NAME) + ".")
    );

    public static final LangText COMMAND_DROP_DONE = LangText.of("Command.Drop.Done",
        LIGHT_GRAY.wrap("Dropped " + LIGHT_YELLOW.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_MIN + " - " + GENERIC_MAX) + " item(s) at " + LIGHT_YELLOW.wrap(LOCATION_X) + ", " + LIGHT_YELLOW.wrap(LOCATION_Y) + ", " + LIGHT_YELLOW.wrap(LOCATION_Z) + " in " + LIGHT_YELLOW.wrap(LOCATION_WORLD) + ".")
    );

    public static final LangText COMMAND_SOUND_DONE = LangText.of("Command.Sound.Done",
        SOUND.wrap(Sound.UI_BUTTON_CLICK),
        LIGHT_GRAY.wrap("Money pickup sound: " + LIGHT_YELLOW.wrap(GENERIC_STATE))
    );

    public static final LangText ERROR_INVALID_BOOSTER = LangText.of("Error.InvalidBooster",
        LIGHT_RED.wrap("Invalid booster!"));

    public static final LangText ERROR_INVALID_CURRENCY = LangText.of("Error.InvalidCurrency",
        LIGHT_RED.wrap("Invalid currency!"));


    public static final LangText CURRENCY_LIMIT_NOTIFY = LangText.of("Currency.Limit.Notify",
        TAG_NO_PREFIX,
        " ",
        LIGHT_RED.wrap(BOLD.wrap("Limit Notification:")),
        LIGHT_GRAY.wrap("You reached daily " + LIGHT_RED.wrap(GENERIC_MAX) + " limit for " + LIGHT_RED.wrap(CURRENCY_NAME) + "."),
        LIGHT_GRAY.wrap("You can't get more " + LIGHT_RED.wrap(CURRENCY_NAME) + " in loot until next day."),
        " "
    );

    public static final LangText CURRENCY_PICKUP = LangText.of("Currency.Pickup",
        OUTPUT.wrap(OutputType.ACTION_BAR),
        LIGHT_GRAY.wrap("You picked up " + LIGHT_GREEN.wrap(GENERIC_AMOUNT) + "! Balance: " + LIGHT_GREEN.wrap(GENERIC_BALANCE))
    );

    public static final LangText CURRENCY_LOST_PENALTY = LangText.of("Currency.Lost.Penalty",
        TAG_NO_PREFIX,
        LIGHT_GRAY.wrap("You paid " + LIGHT_RED.wrap(GENERIC_AMOUNT) + " penalty for " + LIGHT_RED.wrap(GENERIC_NAME) + "!")
    );

    public static final LangText CURRENCY_LOST_DEATH = LangText.of("Currency.Lost.Death",
        TAG_NO_PREFIX,
        LIGHT_GRAY.wrap("You lost " + LIGHT_RED.wrap(GENERIC_AMOUNT) + " on death!"),
        LIGHT_GRAY.wrap("New balance: " + LIGHT_RED.wrap(GENERIC_BALANCE))
    );



    public static final LangString BOOSTER_FORMAT_POSITIVE = LangString.of("Boosters.Format.Positive",
        LIGHT_GREEN.wrap("+" + GENERIC_AMOUNT + "%"));

    public static final LangString BOOSTER_FORMAT_NEGATIVE = LangString.of("Boosters.Format.Negative",
        LIGHT_RED.wrap(GENERIC_AMOUNT + "%"));

    public static final LangText BOOSTER_ACTIVATED_GLOBAL = LangText.of("Booster.Activated.Global",
        TAG_NO_PREFIX + SOUND.wrap(Sound.BLOCK_NOTE_BLOCK_BELL),
        " ",
        LIGHT_YELLOW.wrap(BOLD.wrap("Booster Activated!")),
        LIGHT_YELLOW.wrap("✔ " + LIGHT_GRAY.wrap("Currency Boost: ") + GENERIC_AMOUNT),
        LIGHT_YELLOW.wrap("✔ " + LIGHT_GRAY.wrap("Duration: ") + GENERIC_TIME),
        " "
    );

    public static final LangText BOOSTER_ACTIVATED_PERSONAL = LangText.of("Booster.Activated.Personal",
        LIGHT_GRAY.wrap("You got personal " + LIGHT_GREEN.wrap(GENERIC_AMOUNT) + " currency booster for " + LIGHT_GREEN.wrap(GENERIC_TIME))
    );

    public static final LangText BOOSTER_EXPIRED_GLOBAL = LangText.of("Booster.Expired.Global",
        LIGHT_GRAY.wrap("Global " + LIGHT_YELLOW.wrap(GENERIC_AMOUNT) + " currency booster has been expired.")
    );

    public static final LangText BOOSTER_EXPIRED_PERSONAL = LangText.of("Booster.Expired.Personal",
        LIGHT_GRAY.wrap("Your " + LIGHT_YELLOW.wrap(GENERIC_AMOUNT) + " currency booster has been expired.")
    );

    public static final LangText BOOSTER_LIST_INFO = LangText.of("Booster.List.Info",
        TAG_NO_PREFIX,
        " ",
        LIGHT_YELLOW.wrap(BOLD.wrap("Active Boosters:")),
        GENERIC_ENTRY,
        " ",
        LIGHT_YELLOW.wrap(BOLD.wrap("Total Boost: ")) + LIGHT_GREEN.wrap(GENERIC_TOTAL),
        " "
    );

    public static final LangString BOOSTER_LIST_ENTRY = LangString.of("Booster.List.Entry",
        LIGHT_YELLOW.wrap("✔ " + LIGHT_GRAY.wrap(GENERIC_TYPE + " Booster: ") + GENERIC_AMOUNT + " " + GRAY.wrap("(" + WHITE.wrap(GENERIC_TIME) + ")"))
    );

    public static final LangText BOOSTER_LIST_NOTHING = LangText.of("Booster.List.Nothing",
        LIGHT_RED.wrap("There are no active boosters.")
    );
}
