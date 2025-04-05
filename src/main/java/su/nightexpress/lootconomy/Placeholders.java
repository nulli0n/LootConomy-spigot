package su.nightexpress.lootconomy;

import su.nightexpress.lootconomy.loot.objective.ObjectiveCategory;
import su.nightexpress.nightcore.util.placeholder.PlaceholderList;

public class Placeholders extends su.nightexpress.nightcore.util.Placeholders {

    public static final String URL_WIKI            = "https://github.com/nulli0n/LootConomy-spigot/wiki/";
    public static final String URL_PLACEHOLDERS    = URL_WIKI + "Internal-Placeholders";
    public static final String URL_DATA_VALUES     = "https://minecraft.wiki/w/Java_Edition_data_values";

    public static final String URL_ECO_BRIDGE = "https://nightexpressdev.com/economy-bridge/currencies/";

    public static final String GENERIC_AMOUNT   = "%amount%";
    public static final String GENERIC_NAME     = "%name%";
    public static final String GENERIC_BALANCE  = "%balance%";
    public static final String GENERIC_TIME     = "%time%";
    public static final String GENERIC_CURRENCY = "%currency%";
    public static final String GENERIC_POS      = "%pos%";
    public static final String GENERIC_CURRENT  = "%current%";
    public static final String GENERIC_MIN      = "%min%";
    public static final String GENERIC_MAX      = "%max%";
    public static final String GENERIC_CHANCE   = "%chance%";
    public static final String GENERIC_STATE    = "%state%";
    public static final String GENERIC_TOTAL    = "%total%";
    public static final String GENERIC_PERCENT  = "%percent%";
    public static final String GENERIC_TYPE     = "%type%";

    public static final String CURRENCY_NAME = su.nightexpress.economybridge.Placeholders.CURRENCY_NAME;

    public static final String CATEGORY_ID          = "%category_id%";
    public static final String CATEGORY_NAME        = "%category_name%";
    public static final String CATEGORY_DESCRIPTION = "%category_description%";

    public static final PlaceholderList<ObjectiveCategory> CATEGORY = PlaceholderList.create(list -> list
        .add(CATEGORY_ID, ObjectiveCategory::getId)
        .add(CATEGORY_NAME, ObjectiveCategory::getName)
        .add(CATEGORY_DESCRIPTION, category -> String.join(TAG_LINE_BREAK, category.getDescription()))
    );
}
