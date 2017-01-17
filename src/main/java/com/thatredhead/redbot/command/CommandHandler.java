package com.thatredhead.redbot.command;

import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

import java.util.Arrays;
import java.util.List;

public class CommandHandler {

    private IDiscordClient client;
    private PermissionHandler perms;
    private DataHandler datah;

    private ICommand[] commands;

    public CommandHandler(IDiscordClient client, PermissionHandler perms, DataHandler datah) {
        this.client = client;
        client.getDispatcher().registerListener(this);
        this.perms = perms;
        this.datah = datah;

        commands = new ICommand[]{

        };
    }

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent e) {
        IMessage msg = e.getMessage();

        Arrays.stream(commands)
                .filter(it -> it.triggeredBy(msg.getContent()))
                .filter(it -> perms.hasPermission(it.getPermission(), msg.getAuthor(), msg.getChannel()))
                .forEach(it -> it.invoke(msg));
    }
}
