package net.olympiccode.vhackos.bot.core.misc;

import net.olympiccode.vhackos.bot.core.config.ConfigOption;

public class MiscConfigValues {

    @ConfigOption(path = "misc.enabled", defaultValue = "true", options = {"true", "false"})
    public static boolean enabled;

    @ConfigOption(path = "misc.enableMiner", defaultValue = "true", options={"true", "false"})
    public static boolean enableMiner;

    @ConfigOption(path = "misc.enableMissions", defaultValue = "true", options={"true", "false"})
    public static boolean enableMissions;

    @ConfigOption(path = "misc.doMissionActions", defaultValue = "true", options={"true", "false"})
    public static boolean doMissionActions;


}
