package net.olympiccode.vhackos.bot.core.misc;

import io.sentry.Sentry;
import net.olympiccode.vhackos.api.entities.AppType;
import net.olympiccode.vhackos.api.entities.impl.MissionManagerImpl;
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

        } catch (Exception e) {
            Sentry.capture(e);
            e.printStackTrace();
            miscService.shutdownNow();
            LOG.warn("The misc service has been shutdown due to an error.");
        }
    }

    boolean exploitFinished = false;
    boolean bruteforceFinished = false;
    boolean logFinished = false;
    boolean emptyFinished = false;

    public void runService() {
        try {
            history[5] = history[4];
            history[4] = history[3];
            history[3] = history[2];
            history[2] = history[1];
            history[1] = history[0];
            history[0] = vHackOSBot.api.getLeaderboards().getTournamentRank();

            if (MiscConfigValues.enableMiner) {
                if (vHackOSBot.api.getAppManager().getApp(AppType.NCMiner).isInstalled()) {
                    if (!vHackOSBot.api.getMiner().isRunning()) {
                        if (vHackOSBot.api.getMiner().start()) {
                            LOG.info("Collected and restarted miner");
                        } else {
                            LOG.info("Failed to collect and restart miner");
                        }
                    }
                } else {
                    LOG.warn("MiscService ran but miner was not installed.");
                }
            }

            if (MiscConfigValues.enableMissions) {
                if (vHackOSBot.api.getAppManager().getApp(AppType.Missions).isInstalled()) {
                  vHackOSBot.api.getMissionManager().getDailyMissions().forEach(dailyMission -> {
                      switch (((MissionManagerImpl.DailyMissionImpl) dailyMission).getId()) {
                          case 0:
                              exploitFinished = dailyMission.isFinished() || dailyMission.isClaimed();
                              break;
                          case 1:
                              bruteforceFinished = dailyMission.isFinished() || dailyMission.isClaimed();
                              break;
                          case 2:
                              logFinished = dailyMission.isFinished() || dailyMission.isClaimed();
                              break;
                          case 3:
                              emptyFinished = dailyMission.isFinished() || dailyMission.isClaimed();
                              break;
                          default:
                              throw new RuntimeException("Invalid id: " + ((MissionManagerImpl.DailyMissionImpl) dailyMission).getId());

                      }
                      if (dailyMission.isFinished() && !dailyMission.isClaimed()) {
                          LOG.info("Calimed mission for " + dailyMission.getRewardAmount() + " " + dailyMission.getType() + " and " + dailyMission.getExpReward() + "XP");
                          dailyMission.claim();
                      }
                  });
                }
            }
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
