package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.command.ICommand;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.handle.obj.Permissions;

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
    public String getDescription() {
        return "Sets permissions- currently not working";
    }

    @Override
    public String getUsage() {
        return "will finish eventually";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public PermissionContext getDefaultPermissions() {
        return new PermissionContext(Permissions.ADMINISTRATOR);
    }


    @Override
    public void invoke(MessageParser msgp) {
        if(!"135553137699192832".equals(msgp.getAuthor().getID())) return;


        if(msgp.getArgCount() > 2 && msgp.getArg(1).equals("add")) {
            perms.getOrAdd(msgp.getGuild(),
                    msgp.getArg(2)).addSub(new PermissionContext(msgp.getGuild().getEveryoneRole()));
            perms.save();
        }

        DiscordUtils.sendMessage(perms.toStringForGuild(msgp.getGuild()), msgp.getChannel());
    }
}