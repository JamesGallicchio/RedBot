package com.thatredhead.redbot.command;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public interface ICommand {

    String getKeyword();
    String getPermission();
    void invoke(String msg, IUser user, IChannel channel);
}
