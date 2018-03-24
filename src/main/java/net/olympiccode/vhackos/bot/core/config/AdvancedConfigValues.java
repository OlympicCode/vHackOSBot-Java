package net.olympiccode.vhackos.bot.core.config;

import ch.qos.logback.classic.Level;

public class AdvancedConfigValues {

    @AdvancedConfigOption(path = "loglevel", defaultValue = "info", options = {"all", "debug", "error", "info", "off", "trace", "warn"})
    public static String logLevel;

    @AdvancedConfigOption(path = "login.accesstoken", defaultValue = "---", options = {""})
    public static String token;

    @AdvancedConfigOption(path = "login.uid", defaultValue = "---", options = {""})
    public static String uid;
}
