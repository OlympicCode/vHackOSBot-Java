package net.olympiccode.vhackos.bot.core.networking;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.olympiccode.vhackos.api.entities.AppType;
import net.olympiccode.vhackos.api.entities.BruteForceState;
import net.olympiccode.vhackos.api.entities.impl.BruteForceImpl;
import net.olympiccode.vhackos.api.exceptions.ExploitFailedException;
import net.olympiccode.vhackos.api.network.BruteForce;
import net.olympiccode.vhackos.api.network.ExploitedTarget;
import net.olympiccode.vhackos.api.network.NetworkManager;
import net.olympiccode.vhackos.bot.core.BotService;
import net.olympiccode.vhackos.bot.core.vHackOSBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.nio.channels.NetworkChannel;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NetworkingService implements BotService {
    ScheduledExecutorService networkingService;
    Logger LOG = LoggerFactory.getLogger("NetworkingService");

    public NetworkingService() {
        LOG.info("Creating NetworkingService...");
        networkingService = Executors.newScheduledThreadPool(1, new NetworkingServiceFactory());
    }

    public class NetworkingServiceFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new Thread(r,   "vHackOSBot-NetworkingService");
        }
    }


    public void setup() {
        LOG.info("Setting up NetworkingSerice...");
        networkingService.scheduleAtFixedRate(() -> runService(), 0, 60000, TimeUnit.MILLISECONDS);
    }

    Cache<String, String> cache = CacheBuilder.newBuilder()
            .maximumSize(300)
       .expireAfterWrite(30, TimeUnit.MINUTES).build();

    public void runService() {
        try {
            ((ArrayList<BruteForce>)((ArrayList)vHackOSBot.api.getTaskManager().getActiveBrutes()).clone()).forEach(bruteForce -> {
                if (cache.asMap().containsKey(bruteForce.getIp())) return;
                cache.put(bruteForce.getIp(), "");
                if (bruteForce.getState() == BruteForceState.SUCCESS) {
                    ExploitedTarget etarget = bruteForce.exploit();
                    ExploitedTarget.Banking banking = etarget.getBanking();

                    if (banking.isBruteForced()) {
                        long av = banking.getAvaliableMoney();
                        if (av > 0 && banking.withdraw()) {
                            LOG.info("Withdrawed " + av + " of " + banking.getTotal() + " from " + etarget.getIp() + ".");
                        } else {
                            LOG.error("Failed to withdraw from " + etarget.getIp() + ".");
                        }
                        if (eval(etarget)) {
                            LOG.info("Removing bruteforce from " + etarget.getIp() + ".");
                            bruteForce.remove();
                        }

                    } else {
                        if (banking.startBruteForce()) {
                            LOG.info("Started bruteforce at " + etarget.getIp());
                        } else {
                            LOG.error("Failed to start bruteforce at " + etarget.getIp());
                        }
                    }
                    etarget.setSystemLog(NetworkingConfigValues.logMessage.replaceAll("%username%", vHackOSBot.api.getStats().getUsername()));
                } else if (bruteForce.getState() == BruteForceState.FAILED) {
                    switch (NetworkingConfigValues.onFail) {
                        case "retry":
                            LOG.info("Retrying bruteforce at " + bruteForce.getIp() + " has it failed.");
                            ((BruteForceImpl) bruteForce).retry();
                        case "remove":
                            LOG.info("Removing bruteforce from " + bruteForce.getIp() + " has it failed.");
                            bruteForce.remove();
                    }
                }
            });
            if (vHackOSBot.api.getStats().getExploits() > 0) {
               int success = 0;
               while (success < 6) {
                   success += scan();
               }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int scan() {
        final int[] success = {0};
        vHackOSBot.api.getNetworkManager().getTargets().forEach(target -> {
            if (vHackOSBot.api.getStats().getExploits() <= 0) return;
            if (target.getFirewall() < vHackOSBot.api.getAppManager().getApp(AppType.SDK).getLevel() && !target.isOpen()) {
                success[0]++;
                LOG.info("Exploiting " + target.getIp() + "...");
                try {
                    ExploitedTarget etarget = target.exploit();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LOG.info("Starting bruteforce on " + target.getIp() + "...");
                    if (etarget.getBanking().startBruteForce()) {
                        LOG.info("Started bruteforce on " + target.getIp() + ".");
                    } else {
                        LOG.error("Failed to start bruteforce on " + target.getIp() + ".");
                    }
                    etarget.setSystemLog(NetworkingConfigValues.logMessage.replaceAll("%username%", vHackOSBot.api.getStats().getUsername()));
                } catch (ExploitFailedException e) {
                    LOG.warn("Failed to exploit " + target.getIp() + ": " + e.getMessage());
                }
            }
        });
        return success[0];
    }

    public boolean eval(ExploitedTarget target) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        String foo = NetworkingConfigValues.bruteForceRemove;
        foo = foo.replaceAll("%savings%", String.valueOf(target.getBanking().getSavings()));
        foo = foo.replaceAll("%maxsavings%", String.valueOf(target.getBanking().getMaxSavings()));
        foo = foo.replaceAll("%total%", String.valueOf(target.getBanking().getTotal()));
        try {
            return (boolean) engine.eval(foo);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return false;
    }
}
