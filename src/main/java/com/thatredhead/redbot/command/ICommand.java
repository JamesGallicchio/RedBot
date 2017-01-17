package com.thatredhead.redbot.command;

import sx.blah.discord.handle.obj.IMessage;

public interface ICommand {

    boolean isKeyword(String keyword);
    void invoke(IMessage message);
}
