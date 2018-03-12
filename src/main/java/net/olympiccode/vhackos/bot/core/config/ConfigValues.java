package net.olympiccode.vhackos.bot.core.config;

public class ConfigValues {

    @ConfigOption(path = "username", defaultValue = "***", options = {""})
    public static String username;

    @ConfigOption(path = "password", defaultValue = "***", options = {""})
    public static String password;
}
