package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.command.ICommand;
import com.thatredhead.redbot.permission.PermissionHandler;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

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
    public void invoke(String msg, IUser user, IChannel channel) {
        if(!"135553137699192832".equals(user.getID())) return;
        DiscordUtils.sendMessage(perms.getPerms().toString(), channel);
        if(msg.startsWith("add"))

    }
}
