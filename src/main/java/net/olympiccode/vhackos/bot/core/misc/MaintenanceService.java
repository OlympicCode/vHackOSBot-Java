package net.olympiccode.vhackos.bot.core.misc;

import net.olympiccode.vhackos.bot.core.BotService;
import net.olympiccode.vhackos.bot.core.config.ConfigValues;
import net.olympiccode.vhackos.bot.core.networking.NetworkingConfigValues;
import net.olympiccode.vhackos.bot.core.updating.UpdateConfigValues;
import net.olympiccode.vhackos.bot.core.vHackOSBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MaintenanceService implements BotService {
    ScheduledExecutorService mainService;
    Logger LOG = LoggerFactory.getLogger("MainService");
    public MaintenanceService() {
        LOG.info("Creating MainService...");
        mainService = Executors.newScheduledThreadPool(1, new MainServiceFactory());
    }

    @Override
    public ScheduledExecutorService getService() {
        return mainService;
    }

    @Override
    public void setup() {
        LOG.info("Setting up MainSerice...");
        mainService.scheduleAtFixedRate(() -> runService(), 60000 * 5, 60000 * 5, TimeUnit.MILLISECONDS);
    }

    int miscAttempts = 0;
    int netAttempts = 0;
    int upAttempts = 0;
    @Override
    public void runService() {
       if (MiscConfigValues.enabled && (vHackOSBot.miscService.miscService.isTerminated() || vHackOSBot.miscService.miscService.isTerminated()) && miscAttempts < 4) {
           miscAttempts++;
           LOG.info("Restarting MiscSerivce has it stopped due to an error (Attempt " + miscAttempts + ")");
           vHackOSBot.miscService.setup();
       }
        if (NetworkingConfigValues.enabled && (vHackOSBot.networkingService.getService().isTerminated() || vHackOSBot.networkingService.getService().isTerminated()) && netAttempts < 4) {
            netAttempts++;
            LOG.info("Restarting NetworkingSerivce has it stopped due to an error (Attempt " + netAttempts + ")");
            vHackOSBot.networkingService.setup();
        }
        if (UpdateConfigValues.enabled && (vHackOSBot.updateService.getService().isTerminated() || vHackOSBot.updateService.getService().isTerminated()) && upAttempts < 4) {
            upAttempts++;
            LOG.info("Restarting UpdateSerivce has it stopped due to an error (Attempt " + upAttempts + ")");
            vHackOSBot.updateService.setup();
        }
    }

    public class MainServiceFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new Thread(r, "vHackOSBot-MainService");
        }
    }
}
