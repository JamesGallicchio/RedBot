package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.handle.obj.Permissions;

public class PermsCommand extends Command {

    private PermissionHandler perms;

    public PermsCommand() {
        this.perms = RedBot.getPermHandler();
        keyword = permission = "perms";
        description = "Sets permissions- currently not working";
        usage = "will finish eventually";
    }

    @Override
    public PermissionContext getDefaultPermissions() {
        return new PermissionContext(Permissions.ADMINISTRATOR);
    }


    @Override
    public void invoke(MessageParser msgp) {

        if(msgp.getArgCount() > 2)
            if("add".equals(msgp.getArg(1)))
                perms.getOrAdd(msgp.getGuild(),
                        msgp.getArg(2)).addSub(new PermissionContext(msgp.getGuild().getEveryoneRole()));
            else if("remove".equals(msgp.getArg(1)))
                perms.remove(msgp.getGuild(),
                        msgp.getArg(2));

        DiscordUtils.sendMessage(perms.toStringForGuild(msgp.getGuild()), msgp.getChannel());
    }
}