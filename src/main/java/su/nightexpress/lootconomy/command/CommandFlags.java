package su.nightexpress.lootconomy.command;


import su.nightexpress.nightcore.command.experimental.builder.SimpleFlagBuilder;
import su.nightexpress.nightcore.command.experimental.flag.FlagTypes;

public class CommandFlags {

    public static SimpleFlagBuilder silent() {
        return FlagTypes.simple("s");
    }
}
