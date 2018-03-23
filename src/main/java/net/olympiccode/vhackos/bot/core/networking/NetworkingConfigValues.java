package net.olympiccode.vhackos.bot.core.networking;

import net.olympiccode.vhackos.bot.core.config.ConfigOption;

public class NetworkingConfigValues {

    @ConfigOption(path = "networking.enabled", defaultValue = "true", options = {"true", "false"})
    public static boolean enabled;

    @ConfigOption(path = "networking.bruteforceremove", defaultValue = "\"((%savings% / 2 < %maxsavings%) && %total% < 1000000)\"", options = {""})
    public static String bruteForceRemove;

    @ConfigOption(path = "networking.logMessage", defaultValue = "\"%username% was here. #OlympicCode\"", options = {""})
    public static String logMessage;

    @ConfigOption(path = "networking.onFail", defaultValue = "retry", options = {"retry", "remove"})
    public static String onFail;

    @ConfigOption(path = "networking.withdrawPorcentage", defaultValue = "100", options = {""})
    public static int withdrawPorcentage;

}
