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
        super("perms", "Sets permissions data for this guild",
                "perms [ <set | add | remove | copy> <ID> [ID2 [ID3 ...]] <mention or perm enum>");
        this.perms = RedBot.getPermHandler();
    }

    @Override
    public PermissionContext getDefaultPermissions() {
        return new PermissionContext(Permissions.ADMINISTRATOR);
    }


    @Override
    public void invoke(MessageParser msgp) {

        if(msgp.getArgCount() > 1)
            if(msgp.getArgCount() > 2)
                

        DiscordUtils.sendMessage(perms.toStringForGuild(msgp.getGuild()), msgp.getChannel());
    }
}