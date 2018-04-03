package net.olympiccode.vhackos.bot.core.server;

import net.olympiccode.vhackos.bot.core.config.ConfigOption;

public class ServerConfigValues {

    @ConfigOption(path = "networking.enabled", defaultValue = "true", options = {"true", "false"})
    public static boolean enabled;

}
