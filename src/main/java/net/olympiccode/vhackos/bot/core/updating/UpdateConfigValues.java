package net.olympiccode.vhackos.bot.core.updating;

import net.olympiccode.vhackos.bot.core.config.ConfigOption;

public class UpdateConfigValues {

    @ConfigOption(path = "update.enabled", defaultValue = "true", options = {"true", "false"})
    public static boolean enabled;

    @ConfigOption(path = "update.installAllAvaliableApps", defaultValue = "true", options={"true", "false"})
    public static boolean installAllAvalibaleApps;

    @ConfigOption(path = "update.useBoosters", defaultValue = "true", options={"true", "false"})
    public static boolean useBoosters;

    @ConfigOption(path = "update.finishWithNetcoins", defaultValue = "false", options={"true", "false"})
    public static boolean finishWithNetcoins;

    @ConfigOption(path = "update.minNetcoins", defaultValue = "1000", options = {""})
    public static int minNetcoins;

    @ConfigOption(path = "update.minBoosters", defaultValue = "0", options = {""})
    public static int minBoosters;

    @ConfigOption(path = "update.prioritymode", defaultValue = "lowerfirst", options={"lowerfirst", "higherfirst", "random"})
    public static String priorityMode;

    @ConfigOption(path = "update.listmode", defaultValue = "blacklist", options = {"whitelist", "blacklist"})
    public static String listMode;

    @ConfigOption(path = "update.list", defaultValue = "[]", options = {""})
    public static String[] updateList;

    @ConfigOption(path = "update.boostMinimumMinutes", defaultValue = "20", options = {""})
    public static int boostMinimumMinutes;

}
