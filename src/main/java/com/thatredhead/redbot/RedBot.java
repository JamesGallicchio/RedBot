package com.thatredhead.redbot;

import com.thatredhead.redbot.command.CommandHandler;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionHandler;
import org.apache.commons.io.FileUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class RedBot {

    public static final String INVITE = "https://discordapp.com/oauth2/authorize?client_id=270057017966723083&scope=bot&permissions=68672";

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

    public static DataHandler getDataHandler() {
        if(ready)
            return datah;
        throw new NotReadyException();
    }

    public static IDiscordClient getClient() {
        if(ready)
            return client;
        throw new NotReadyException();
    }

    public static PermissionHandler getPermHandler() {
        if(ready)
            return permh;
        throw new NotReadyException();
    }

    public static String getUptime() {
        if(ready) {
            int seconds = (int) ((System.currentTimeMillis() - startup)/1000);
            return String.format("%02d:%02d:%02d",
                    seconds/3600,
                    seconds/60%60,
                    seconds%60);
        }
        throw new NotReadyException();
    }

    public static String getVersion() {
        return version;
    }

    public static class NotReadyException extends RuntimeException {}
}