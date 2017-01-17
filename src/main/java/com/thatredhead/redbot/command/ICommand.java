package com.thatredhead.redbot.command;

import sx.blah.discord.handle.obj.IMessage;

public interface ICommand {

    boolean triggeredBy(String str);
    String getPermission();
    void invoke(IMessage msg);
}
