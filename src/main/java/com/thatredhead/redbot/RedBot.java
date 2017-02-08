package com.thatredhead.redbot;

import com.thatredhead.redbot.command.CommandHandler;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionHandler;
import org.apache.commons.io.FileUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;

import java.io.File;

public class RedBot {

    private static IDiscordClient client;
    private static DataHandler datah;
    private static PermissionHandler permh;

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

            datah = new DataHandler();
            permh = datah.getPermHandler();
            new CommandHandler(client, datah);
        } catch (Exception e) {
            System.out.println("Failed login:");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static DataHandler getDataHandler() {
        return datah;
    }

    public static IDiscordClient getClient() {
        return client;
    }

    public static PermissionHandler getPermHandler() {
        return permh;
    }
}