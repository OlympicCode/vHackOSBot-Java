package net.olympiccode.vhackos.bot.core.server;

import io.sentry.Sentry;
import net.olympiccode.vhackos.api.entities.AppType;
import net.olympiccode.vhackos.api.entities.impl.ServerImpl;
import net.olympiccode.vhackos.api.server.Server;
import net.olympiccode.vhackos.bot.core.BotService;
import net.olympiccode.vhackos.bot.core.vHackOSBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ServerService implements BotService {

    public static ScheduledExecutorService serverService;
    Logger LOG = LoggerFactory.getLogger("ServerService");

    public ServerService() {
        LOG.info("Creating ServerService...");
        serverService = Executors.newScheduledThreadPool(1, new ServerServiceFactory());
    }


    @Override
    public ScheduledExecutorService getService() {
        return serverService;
    }

    @Override
    public void setup() {
        LOG.info("Setting up ServerSerice...");
        if (serverService.isTerminated() || serverService.isShutdown()) {
            serverService = Executors.newScheduledThreadPool(1, new ServerServiceFactory());
        }
        if (vHackOSBot.api.getAppManager().getApp(AppType.Server).isInstalled()) {
            serverService.scheduleAtFixedRate(() -> runService(), 0, 330000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void runService() {
        try {
            Server server = vHackOSBot.api.getServer();

            if (server.getPackages() > 0) {
                Server.OpenResult result = server.openAllPacks();
                LOG.info("Opened " + server.getPackages() + " server packages, got " + result.getServer() + " server, " + result.getAv() + " av, " + result.getFw() + " fw and " + result.getBoost() + " boosters.");
            }
            if (ServerConfigValues.upgradeNodes) {
                server.getNodes().forEach(serverNode -> {
                   while (serverNode.getMaxStrength() > serverNode.getStrength() && has(serverNode, server) != 0) {
                       int s = serverNode.getStrength();
                       boolean b = false;
                       if (has(serverNode, server) == 1) b = serverNode.upgrade(); else if (has(serverNode, server) == 2) b = serverNode.upgradeFive();
                       ((ServerImpl) server).update();
                       if (b) LOG.info("Upgraded " + serverNode.getType() + " node (" + s + "->" + serverNode.getStrength() + ")"); else LOG.info("Failed to upgrade " + serverNode.getType() + " node.");
                   }
                });
            }

        } catch (Exception e) {
            Sentry.capture(e);
            LOG.warn("The server service has been shutdown due to an error.");
            e.printStackTrace();
            serverService.shutdownNow();
        }
    }

    int has(Server.ServerNode serverNode, Server server) {
        switch (serverNode.getType()) {
            case AV:
                if (server.getAntivirusPieces() > 0) return (server.getAntivirusPieces() > 4) ? 2 : 1;
                break;
            case FW:
                if (server.getFirewallPieces() > 0) return (server.getFirewallPieces() > 4) ? 2 : 1;
                break;
            case SERVER:
                if (server.getServerPieces() > 0) return (server.getServerPieces() > 4) ? 2 : 1;
                break;
        }
        return 0;
    }

    public class ServerServiceFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new Thread(r, "vHackOSBot-ServerService");
        }
    }
}
