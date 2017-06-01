package com.thatredhead.redbot;

import com.thatredhead.redbot.command.CommandHandler;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.econ.Economy;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;
import sx.blah.discord.util.RequestBuffer;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class of RedBot, creates instance of itself
 */
public class RedBot {

    public static final String INVITE = "https://goo.gl/WcN0QK";
    public static final long OWNER_ID = 135553137699192832L;
    public static final long ERROR_CHANNEL_ID = 287324375785537547L;

    public static final String DEFAULT_PREFIX = "%";

    public static final String PERM_FILE_NAME = "perms";
    public static final String ECON_FILE_NAME = "econ";

    public static final Logger LOGGER = LoggerFactory.getLogger(RedBot.class);

    public static final ProcessBuilder GIT = new ProcessBuilder("bash", "update.sh");
    public static final ProcessBuilder MVN = new ProcessBuilder("bash", "build.sh");
    public static final ProcessBuilder RUN_REDBOT = new ProcessBuilder("bash", "run.sh");

    public static void main(String[] args) {

        if(args.length == 0) {
            try {
                new RedBot(FileUtils.readFileToString(new File("test/test_token.txt"), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                new RedBot(args[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static IDiscordClient client;

    private static DataHandler datah;
    private static PermissionHandler permh;
    private static CommandHandler cmdh;
    private static Economy econ;

    private static long startup;
    private static String version;
    private static AtomicBoolean ready = new AtomicBoolean(false);

    public RedBot() {

    }

    public RedBot(String token) {
        try {
            client = new ClientBuilder()
                    .withToken(token)
                    .login();

            client.getDispatcher().waitFor(ReadyEvent.class);

            ready.set(true);
        } catch (Exception e) {
            System.out.println("Failed login:");
            e.printStackTrace();
            System.exit(0);
        }

        datah = new DataHandler();

        permh = datah.get(PERM_FILE_NAME, PermissionHandler.class);
        if (permh == null) {
            permh = new PermissionHandler();
            datah.save(permh, PERM_FILE_NAME);
        }

        econ = datah.get(ECON_FILE_NAME, Economy.class);
        if (econ == null) {
            econ = new Economy();
            datah.save(econ, ECON_FILE_NAME);
        }


        startup = System.currentTimeMillis();

        try {
            System.getProperties().load(getClass().getClassLoader().getResourceAsStream("redbot.properties"));
            version = System.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Reflections("com.thatredhead.redbot", new MethodAnnotationsScanner()).getMethodsAnnotatedWith(EventSubscriber.class).stream()
                .map(method -> {
                    try {
                        if (method.getDeclaringClass().equals(CommandHandler.class)) {
                            return (cmdh = (CommandHandler) method.getDeclaringClass().newInstance());
                        } else
                            return method.getDeclaringClass().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull).distinct().forEach(client.getDispatcher()::registerListener);
    }

    @EventSubscriber
    public static void onSuccessfulReconnect(ReconnectSuccessEvent event) {
        ready.set(true);
    }

    @EventSubscriber
    public static void onDisconnect(DisconnectedEvent event) {
        ready.set(false);
    }

    public static boolean isReady() {
        return ready.get();
    }

    /**
     * Gets the DataHandler for this RedBot instance
     *
     * @return DataHandler instance for this object
     */
    public static DataHandler getDataHandler() {
        if (ready.get())
            return datah;
        throw new NotReadyException();
    }

    /**
     * Gets the IDiscordClient for this RedBot instance
     *
     * @return DataHandler instance for this object
     */
    public static IDiscordClient getClient() {
        if (ready.get())
            return client;
        throw new NotReadyException();
    }

    /**
     * Gets the PermissionHandler for this RedBot instance
     *
     * @return PermissionHandler instance for this object
     */
    public static PermissionHandler getPermHandler() {
        if (ready.get())
            return permh;
        throw new NotReadyException();
    }

    public static CommandHandler getCommandHandler() {
        if (ready.get())
            return cmdh;
        throw new NotReadyException();
    }

    /**
     * Gets the Economy for this RedBot instance
     *
     * @return Economy instance for this object
     */
    public static Economy getEconomy() {
        if (ready.get())
            return econ;
        throw new NotReadyException();
    }

    /**
     * Gets the length of time this RedBot instance has been online
     *
     * @return uptime in H..H:MM:SS format
     */
    public static String getUptime() {
        if (ready.get()) {
            int seconds = (int) ((System.currentTimeMillis() - startup) / 1000);
            return String.format("%d:%02d:%02d",
                    seconds / 3600,
                    seconds / 60 % 60,
                    seconds % 60);
        }
        throw new NotReadyException();
    }

    /**
     * Gets the maven version for this RedBot instance
     *
     * @return version in Major.minor.revision format
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Exception thrown when getters are called before the RedBot client is ready
     */
    public static class NotReadyException extends RuntimeException {
    }

    /**
     * Reports an error to both console log and PM to owner
     *
     * @param e exception to report
     */
    public static void reportError(Throwable e) {
        String stacktrace = ExceptionUtils.getStackTrace(e);
        LOGGER.error(stacktrace);
        RequestBuffer.request(() -> client.getChannelByID(ERROR_CHANNEL_ID).sendMessage("```\n" + limit(stacktrace, 1990) + "```"));
    }

    private static String limit(String s, int chars) {
        if (s.length() > chars)
            return s.substring(0, chars);
        return s;
    }

    public static void shutdown() {
        client.logout();
        LogHandler.saveLogFile();
        System.exit(0);
    }

    public static void restart() {

        client.logout();
        LogHandler.saveLogFile();
        try {
            RUN_REDBOT.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static void rebuild() {
        try {
            if (GIT.start().waitFor() != 0)
                throw new IOException("RedBot could not pull changes to the repository!");
            else
                LOGGER.debug("Git pull successful");

        } catch (IOException | InterruptedException e) {
            reportError(e);
        }

        try {
            if (MVN.start().waitFor() != 0)
                throw new IOException("Maven failed to build RedBot!");
            else
                LOGGER.debug("Maven built successfully.");

        } catch (IOException | InterruptedException e) {
            reportError(e);
        }
    }
}