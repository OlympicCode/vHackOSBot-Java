package net.olympiccode.vhackos.bot.core.server;

import net.olympiccode.vhackos.bot.core.config.ConfigOption;

public class ServerConfigValues {

    @ConfigOption(path = "server.enabled", defaultValue = "true", options = {"true", "false"})
    public static boolean enabled;

    @ConfigOption(path = "server.upgradeNodes", defaultValue = "true", options = {"true", "false"})
    public static boolean upgradeNodes;

    @ConfigOption(path = "server.addNewNodes", defaultValue = "true", options = {"true", "false"})
    public static boolean addNewNodes;

}
