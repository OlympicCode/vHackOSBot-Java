package net.olympiccode.vhackos.bot.core.misc;

import net.olympiccode.vhackos.api.entities.AppType;
import net.olympiccode.vhackos.api.network.ExploitedTarget;
import net.olympiccode.vhackos.bot.core.BotService;
import net.olympiccode.vhackos.bot.core.vHackOSBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MiscService implements BotService {
    ScheduledExecutorService miscService;
    Logger LOG = LoggerFactory.getLogger("MiscService");

    public MiscService() {
        LOG.info("Creating MiscService...");
        miscService = Executors.newScheduledThreadPool(1, new MiscServiceFactory());
    }

    public void setup() {
        LOG.info("Setting up MiscSerice...");
        miscService.scheduleAtFixedRate(() -> runService(), 0, 60000 * 60, TimeUnit.MILLISECONDS);
    }

    public void runService() {
        try {
            if (MiscConfigValues.enableMiner) {
                if (vHackOSBot.api.getAppManager().getApp(AppType.NCMiner).isInstalled()) {
                    if (vHackOSBot.api.getMiner().start()) {
                        LOG.info("Collected and restarted miner");
                    } else {
                        LOG.info("Failed to collect and restart miner");
                    }
                } else {
                    LOG.warn("MiscService ran but miner was not installed.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MiscServiceFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new Thread(r, "vHackOSBot-MiscService");
        }
    }

}
