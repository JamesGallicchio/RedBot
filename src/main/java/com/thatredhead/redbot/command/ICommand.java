package com.thatredhead.redbot.command;

public interface ICommand {

    String getKeyword();
    String getPermission();
    void invoke(MessageParser msgp) throws CommandException;
}
