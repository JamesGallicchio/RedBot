package com.thatredhead.redbot.command;

import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public class CommandHandler {

    private IDiscordClient client;
    private PermissionHandler perms;


    public CommandHandler(IDiscordClient client, PermissionHandler perms) {
        this.client = client;
        client.getDispatcher().registerListener(this);
        this.perms = perms;
    }

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent e) {

    }
}
