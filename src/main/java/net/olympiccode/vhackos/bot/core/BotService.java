package net.olympiccode.vhackos.bot.core;

import java.util.concurrent.ScheduledExecutorService;

public interface BotService {
    ScheduledExecutorService getService();
    void setup();
    void runService();
}
