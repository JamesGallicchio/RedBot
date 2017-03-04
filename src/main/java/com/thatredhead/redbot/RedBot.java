package com.thatredhead.redbot;

import com.thatredhead.redbot.data.DataHandler;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

/**
 * Main class of RedBot, creates instance of itself
 */
public class RedBot {

    public static final String INVITE = "https://goo.gl/WcN0QK";
    public static final String OWNER_ID = "135553137699192832";
    public static final String ERROR_CHANNEL_ID = "287324375785537547";

    public static final Logger LOGGER = LoggerFactory.getLogger(RedBot.class);
    private static final long MILLI_LOG_PERSISTENCE = 1000L*60*60*24*7; // 7 days

    static {
        File latest = new File("logs/latest.log");

        if(latest.exists()) latest.delete();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (latest.exists()) {
                try {
                    Files.copy(latest.toPath(),
                            Paths.get("logs/" +
                                    new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(
                                            new Date(latest.lastModified())) +
                                    ".log"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            latest.deleteOnExit();
            for (File f : new File("logs").listFiles())
                if (System.currentTimeMillis() - f.lastModified() > MILLI_LOG_PERSISTENCE)
                    f.deleteOnExit();
        }));
    }

    private static IDiscordClient client;
    private static DataHandler datah;
    private static PermissionHandler permh;
    private static long startup;
    private static String version;
    private static boolean ready = false;

    public static void main(String[] args) {

        try {
            new RedBot(FileUtils.readFileToString(new File("token.txt"), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RedBot(String token) {
        try {
            client = new ClientBuilder()
                    .withToken(token)
                    .login();

            client.getDispatcher().waitFor(ReadyEvent.class);

            ready = true;
        } catch (Exception e) {
            System.out.println("Failed login:");
            e.printStackTrace();
            System.exit(0);
        }

        datah = new DataHandler();
        permh = datah.getPermHandler();
        startup = System.currentTimeMillis();
        try {
            Properties p = new Properties();
            p.load(getClass().getClassLoader().getResourceAsStream("version.properties"));
            version = (String) p.get("version");
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Reflections("com.thatredhead.redbot", new MethodAnnotationsScanner()).getMethodsAnnotatedWith(EventSubscriber.class).stream()
                .map(method -> {
                    try {
                        return method.getDeclaringClass().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull).distinct().forEach(client.getDispatcher()::registerListener);
    }

    /**
     * Gets the DataHandler for this RedBot instance
     * @return DataHandler instance for this object
     */
    public static DataHandler getDataHandler() {
        if(ready)
            return datah;
        throw new NotReadyException();
    }

    /**
     * Gets the IDiscordClient for this RedBot instance
     * @return DataHandler instance for this object
     */
    public static IDiscordClient getClient() {
        if(ready)
            return client;
        throw new NotReadyException();
    }

    /**
     * Gets the PermissionHandler for this RedBot instance
     * @return PermissionHandler instance for this object
     */
    public static PermissionHandler getPermHandler() {
        if(ready)
            return permh;
        throw new NotReadyException();
    }

    /**
     * Gets the length of time this RedBot instance has been online
     * @return uptime in H..H:MM:SS format
     */
    public static String getUptime() {
        if(ready) {
            int seconds = (int) ((System.currentTimeMillis() - startup)/1000);
            return String.format("%d:%02d:%02d",
                    seconds/3600,
                    seconds/60%60,
                    seconds%60);
        }
        throw new NotReadyException();
    }

    /**
     * Gets the maven version for this RedBot instance
     * @return version in Major.minor.revision format
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Exception thrown when getters are called before the RedBot client is ready
     */
    public static class NotReadyException extends RuntimeException {}

    /**
     * Reports an error to both console log and PM to owner
     * @param e exception to report
     */
    public static void reportError(Throwable e) {
        String stacktrace = ExceptionUtils.getStackTrace(e);
        DiscordUtils.sendMessage("```\n" + limit(stacktrace, 1990) + "```", getClient().getChannelByID(ERROR_CHANNEL_ID));
        LOGGER.error(stacktrace);
    }

    private static String limit(String s, int chars) {
        if(s.length() > chars)
            return s.substring(0, chars);
        return s;
    }
}