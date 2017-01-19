package com.thatredhead.redbot.command;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.data.DataHandler;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.Arrays;
import java.util.HashMap;

public class CommandHandler {

    private IDiscordClient client;
    private PermissionHandler perms;
    private DataHandler datah;

    private ICommand[] commands;
    private ICommand[] noPrefixCommands;

    private HashMap<IGuild, String> prefixes;

    public CommandHandler(IDiscordClient client, DataHandler datah) {
        this.client = client;
        client.getDispatcher().registerListener(this);
        this.perms = datah.getPermHandler();
        this.datah = datah;

        prefixes = datah.get("guildprefixes", HashMap.class, new HashMap<IGuild, String>());

        commands = new ICommand[]{
            new DnDCommands.Roll()
        };

        noPrefixCommands = new ICommand[] {

        };
    }

    @EventSubscriber
    public void onMessageReceive(MessageReceivedEvent e) {
        IMessage msg = e.getMessage();

        String content = msg.getContent();
        String prefix = prefixes.get(msg.getGuild());
        if(content.startsWith(prefix)) {
            content = content.substring(prefix.length());
            boolean success = false;
            for (ICommand c : commands) {
                String keyword = c.getKeyword();
                if(content.startsWith(keyword)) {
                    if(perms.hasPermission(c.getPermission(), msg.getAuthor(), msg.getChannel()))
                        c.invoke(content.substring(keyword.length()), msg.getAuthor(), msg.getChannel());
                    else
                        DiscordUtils.sendTemporaryMessage("You don't have permission to perform this command.", msg.getChannel());
                    success = true;
                }
            }
            if(!success)
                DiscordUtils.sendTemporaryMessage("Unknown command! Use help command for a list of commands.", msg.getChannel());
        }

        Arrays.stream(noPrefixCommands)
                .filter(it -> perms.hasPermission(it.getPermission(), msg))
                .forEach(it -> it.invoke(msg.getContent(), msg.getAuthor(), msg.getChannel()));
    }
}