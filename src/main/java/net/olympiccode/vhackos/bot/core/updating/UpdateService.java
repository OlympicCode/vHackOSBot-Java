package net.olympiccode.vhackos.bot.core.updating;

import io.sentry.Sentry;
import net.olympiccode.vhackos.api.appstore.App;
import net.olympiccode.vhackos.api.appstore.Task;
import net.olympiccode.vhackos.api.entities.AppType;
import net.olympiccode.vhackos.api.entities.impl.UpdateableAppImpl;
import net.olympiccode.vhackos.bot.core.BotService;
import net.olympiccode.vhackos.bot.core.config.ConfigOption;
import net.olympiccode.vhackos.bot.core.vHackOSBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class UpdateService implements BotService {
ScheduledExecutorService updateService;
    Logger LOG = LoggerFactory.getLogger("UpdateService");
    public UpdateService() {
        LOG.info("Creating UpdateService...");
       updateService = Executors.newScheduledThreadPool(1, new UpdateServiceFactory());
    }

    @Override
    public ScheduledExecutorService getService() {
        return updateService;
    }

    public void setup() {
        LOG.info("Setting up UpdateSerice...");
        updateService.scheduleAtFixedRate(()->runService(), 0, 30000, TimeUnit.MILLISECONDS);
    }

    public class UpdateServiceFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new Thread(r,   "vHackOSBot-UpdateService");
        }
    }

    public App getNextApp() {
        final App[] type = new App[1];
        switch (UpdateConfigValues.priorityMode) {
            case "lowerfirst":
                final int[] level = {Integer.MAX_VALUE};
                vHackOSBot.api.getAppManager().getApps().forEach(app -> {
                    if (app.isInstalled() &&  !app.isOneTime() && app.getLevel() < level[0] && isListed(app.getType())) {
                        type[0] = app;
                        level[0] = app.getLevel();
                    }
                });
                break;
            case "higherfirst":
                final int[] level2 = {Integer.MIN_VALUE};
                vHackOSBot.api.getAppManager().getApps().forEach(app -> {
                    if (app.isInstalled() &&  !app.isOneTime() && app.getLevel() > level2[0] && isListed(app.getType())) {
                        type[0] = app;
                        level2[0] = app.getLevel();
                    }
                });
                break;
            case "random":
                List<App> apps = vHackOSBot.api.getAppManager().getApps();
                while (type == null) {
                    App app = apps.get((int) (Math.random() * apps.size() + 1));;
                    if (app.isInstalled() &&  !app.isOneTime() && isListed(app.getType())) {
                        type[0] = app;
                    }
                }
                break;
        }
        if (type[0] != null) return type[0];
        return null;
    }

    public boolean isListed(AppType type) {
        switch (UpdateConfigValues.listMode) {
            case "whitelist":
                return Arrays.asList(UpdateConfigValues.updateList).contains(type.getName()) || Arrays.asList(UpdateConfigValues.updateList).contains(type.getId());
            case "blacklist":
                return !Arrays.asList(UpdateConfigValues.updateList).contains(type.getName()) || !Arrays.asList(UpdateConfigValues.updateList).contains(type.getId());
        }
        return false;
    }

    public void runService() {
        try {
            vHackOSBot.api.getAppManager().getApps().forEach(app -> {
                if (!app.isInstalled() && app.getRequiredLevel() <= vHackOSBot.api.getStats().getLevel() && app.getPrice() < vHackOSBot.api.getStats().getMoney()) {
                    if (app.getAsInstallable().install()) {
                        LOG.info("Installed " + app.getType() + " for " + app.getPrice() + "$");
                    }
                }
            });
            List<Task> activetasks = vHackOSBot.api.getTaskManager().getActiveTasks();
            if (activetasks.size() < 1) {
                App app = getNextApp();
                LOG.info("Starting update of " + app.getType() + " for " + app.getPrice() + ".");
                if (app.getAsUpdateable().fillTasks()) {
                    proccessBoosts();
                } else {
                    LOG.warn("Failed to start update of " + app.getType() + " could be out of money or there are already tasks running.");
                }
            } else {
                LOG.info("There are already updates running, trying to boost them..");
                proccessBoosts();
            }
        } catch (Exception e) {
            Sentry.capture(e);
            e.printStackTrace();
            updateService.shutdownNow();
            LOG.warn("The update service has been shutdown due to an error.");
        }
    }

    void proccessBoosts() {
        if (UpdateConfigValues.useBoosters && vHackOSBot.api.getTaskManager().getBoosters() > 0) {
            if (vHackOSBot.api.getTaskManager().boostAll()) {
                LOG.info("Boosted the update.");
            } else {
                LOG.error("Failed to boost the update.");
            }
        }
        if (UpdateConfigValues.finishWithNetcoins) {
            if (UpdateConfigValues.minNetcoins < vHackOSBot.api.getStats().getNetcoins()) {
                int cost = vHackOSBot.api.getTaskManager().getFinishAllCost();
                if (vHackOSBot.api.getStats().getNetcoins() >= cost) {
                    if (vHackOSBot.api.getTaskManager().finishAll()) {
                        LOG.info("Finished all tasks for " + cost + " netcoins.");
                        runService();
                    } else {
                        LOG.error("Failed to finish tasks with netcoins.");
                    }
                }
            }
        }
    }
}
