package net.olympiccode.vhackos.bot.core.config;

import ch.qos.logback.classic.Level;

public class AdvancedConfigValues {

    @AdvancedConfigOption(path = "loglevel", defaultValue = "info", options = {"all", "debug", "error", "info", "off", "trace", "warn"})
    public static String logLevel;

    @AdvancedConfigOption(path = "networking.runTime", defaultValue = "3000", options = {""})
    public static int a;
}
