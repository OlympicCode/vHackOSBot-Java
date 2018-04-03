package net.olympiccode.vhackos.bot.core.misc;

import io.sentry.Sentry;
import net.olympiccode.vhackos.api.entities.AppType;
import net.olympiccode.vhackos.bot.core.BotService;
import net.olympiccode.vhackos.bot.core.vHackOSBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
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

    @Override
    public ScheduledExecutorService getService() {
        return miscService;
    }

    public void setup() {
        LOG.info("Setting up MiscSerice...");
        if (miscService.isTerminated() || miscService.isShutdown()) {
            miscService = Executors.newScheduledThreadPool(1, new MiscServiceFactory());
        }
        miscService.scheduleAtFixedRate(() -> runLongService(), 0, 60000 * 60 + 5000 * 60, TimeUnit.MILLISECONDS);
        miscService.scheduleAtFixedRate(() -> runService(), 0, 60000 * 5, TimeUnit.MILLISECONDS);
    }
    public static int[] history = {0, 0, 0, 0, 0, 0};
    public void runLongService() {
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
            Sentry.capture(e);
            e.printStackTrace();
            miscService.shutdownNow();
            LOG.warn("The misc service has been shutdown due to an error.");
        }
    }

    public void runService() {
        try {
            history[5] = history[4];
            history[4] = history[3];
            history[3] = history[2];
            history[2] = history[1];
            history[1] = history[0];
            history[0] = vHackOSBot.api.getLeaderboards().getTournamentRank();
        } catch (Exception e) {
            Sentry.capture(e);
            e.printStackTrace();
            miscService.shutdownNow();
            LOG.warn("The misc service has been shutdown due to an error.");
        }
    }

    public class MiscServiceFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new Thread(r, "vHackOSBot-MiscService");
        }
    }

}
