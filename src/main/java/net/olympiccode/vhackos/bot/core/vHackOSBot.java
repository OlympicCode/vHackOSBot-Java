package net.olympiccode.vhackos.bot.core;

import ch.qos.logback.classic.Level;
import net.olympiccode.vhackos.api.vHackOSAPI;
import net.olympiccode.vhackos.api.vHackOSAPIBuilder;
import net.olympiccode.vhackos.bot.core.config.AdvancedConfigFile;
import net.olympiccode.vhackos.bot.core.config.AdvancedConfigValues;
import net.olympiccode.vhackos.bot.core.config.ConfigFile;
import net.olympiccode.vhackos.bot.core.config.ConfigValues;
import net.olympiccode.vhackos.bot.core.misc.MiscConfigValues;
import net.olympiccode.vhackos.bot.core.misc.MiscService;
import net.olympiccode.vhackos.bot.core.networking.NetworkingConfigValues;
import net.olympiccode.vhackos.bot.core.networking.NetworkingService;
import net.olympiccode.vhackos.bot.core.updating.UpdateConfigValues;
import net.olympiccode.vhackos.bot.core.updating.UpdateService;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class vHackOSBot {

    public static vHackOSAPI api;
    ConfigFile config = new ConfigFile();
    AdvancedConfigFile advConfig = new AdvancedConfigFile();
    BotService updateService = new UpdateService();
    BotService miscService = new MiscService();
    BotService networkingService = new NetworkingService();
    Logger LOG = LoggerFactory.getLogger("vHackOSBot");

    public static void main(String[] args) {
        new vHackOSBot().run();
    }

    public void run() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down...");
            config.save();
            advConfig.save();
        }));

        advConfig.setupConfig();
        config.setupConfig();
//        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
      //  root.setLevel(Level.valueOf(AdvancedConfigValues.logLevel));
        if (ConfigValues.username.equals("***") || ConfigValues.password.equals("***")) {
            LOG.error("Please set your login data in the config file");
            System.exit(0);
        }
        try {
            api = new vHackOSAPIBuilder().setUsername(ConfigValues.username).setPassword(ConfigValues.password).buildBlocking();
        } catch (LoginException e) {
            LOG.error("vHack returned invalid username/password.");
        } catch (InterruptedException e) {
            LOG.error("There was a problem initializing the vHackOSBot.");
        }


        if (UpdateConfigValues.enabled) updateService.setup();
        if (MiscConfigValues.enabled) miscService.setup();
        if (NetworkingConfigValues.enabled) networkingService.setup();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while (true) {
            try {
                String SEPARATOR = "     ";
                line = in.readLine();
                if (line == null) continue;
                String[] args = line.split(" ");
                switch (args[0]) {
                    case "help":
                        System.out.println("stats - List all basic stats\n" + "tasks - Lists all active tasks");
                        break;
                    case "stats":
                        System.out.println("Username: " + api.getStats().getUsername() + SEPARATOR + "Money: " + api.getStats().getMoney() + SEPARATOR + "Netcoins: " + api.getStats().getNetcoins() +
                                "\n" + "Exploits: " + api.getStats().getExploits() + SEPARATOR + "IP: " + api.getStats().getIpAddress() + "\n" +
                                "Level: " + api.getStats().getLevel() + getProgressBar());
                        break;
                    case "tasks":
                        System.out.println("-------------------\n" + "Boosters: " +  api.getTaskManager().getBoosters() + "-------------------\n" + api.getTaskManager().getActiveTasks().stream().map(task -> task.getType() + ": " + task.getLevel() + " " + ((task.getEndTimestamp() - System.currentTimeMillis()) / 1000) + "sec left.").collect(Collectors.joining("\n")) + "\n-------------------");
                        break;
                    case "brutes":
                        System.out.println("-------------------\n" + api.getTaskManager().getActiveBrutes().stream().map(bruteForce -> bruteForce.getUsername() + ": " + bruteForce.getState()).collect(Collectors.joining("\n")) + "\n-------------------");
                        break;
                    case "quit":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Unkown command, use \"help\" to list all commands.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getProgressBar() {
        long barnum = (Math.round(api.getStats().getLevelPorcentage() / 10)) - 1;
        StringBuilder builder = new StringBuilder();
        builder.append("    [");
        for (int i = 0; i < 10; i++) {
            if (i <= barnum) {
                builder.append("||");
            } else {
                builder.append("  ");
            }
        }
        builder.append("] " + api.getStats().getLevelPorcentage() + "%");
        return builder.toString();
    }
}
