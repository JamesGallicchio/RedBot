package com.thatredhead.redbot;

import com.thatredhead.redbot.command.CommandHandler;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionHandler;
import org.apache.commons.io.FileUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Main class of RedBot, creates instance of itself
 */
public class RedBot {

    public static final String INVITE = "https://goo.gl/WcN0QK";
    public static final String OWNER_ID = "135553137699192832";

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

        new CommandHandler();
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
    public static void reportError(Exception e) {
        e.printStackTrace();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintWriter(bos));
        DiscordUtils.sendPrivateMessage(bos.toString(), getClient().getUserByID(OWNER_ID));
    }
}