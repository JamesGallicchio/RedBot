package com.thatredhead.redbot.command;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public interface IMessageCommand {

    String getKeyword();
    String getPermission();
    void invoke(IMessage msg, IUser user, IChannel channel);
}
