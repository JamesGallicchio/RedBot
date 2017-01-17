package com.thatredhead.redbot;

import com.thatredhead.redbot.command.CommandHandler;
import org.apache.commons.io.FileUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

import java.io.File;

public class RedBot {

    private IDiscordClient client;

    public static void main(String[] args) {

        try {
            new RedBot(FileUtils.readFileToString(new File("client.login"), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RedBot(String token) {
        try {
            client = new ClientBuilder().withToken(token).login();
        } catch (Exception e) {
            System.out.println("Failed login:");
            e.printStackTrace();
            System.exit(0);
        }

        new CommandHandler(client);
    }
}