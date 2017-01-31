package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.command.ICommand;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.api.IDiscordClient;

public class PermsCommand implements ICommand {

    private PermissionHandler perms;

    public PermsCommand(PermissionHandler perms) {
        this.perms = perms;
    }

    @Override
    public String getKeyword() {
        return "perms";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public void invoke(MessageParser msgp) {
        if(!"135553137699192832".equals(msgp.getAuthor().getID())) return;

        if(msgp.getArg(0).equals("add")) {
            
            if(msgp.getArgCount() > 1)
                perms.getOrAdd(msgp.getGuild(), msgp.getArg(1)).addSub(new PermissionContext(msgp.getGuild().getEveryoneRole()));
        }

        DiscordUtils.sendMessage(perms.toStringForGuild(msgp.getGuild()), msgp.getChannel());
    }
}