package com.thatredhead.redbot;

import com.thatredhead.redbot.command.CommandHandler;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionHandler;
import org.apache.commons.io.FileUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import java.io.File;

public class RedBot {

    public static void main(String[] args) {

        try {
            new RedBot(FileUtils.readFileToString(new File("token.txt"), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RedBot(String token) {
        IDiscordClient client;
        try {
            client = new ClientBuilder().withToken(token).login();

            DataHandler datah = new DataHandler();
            new CommandHandler(client, datah.getPermHandler(), datah);
        } catch (Exception e) {
            System.out.println("Failed login:");
            e.printStackTrace();
            System.exit(0);
        }
    }
}