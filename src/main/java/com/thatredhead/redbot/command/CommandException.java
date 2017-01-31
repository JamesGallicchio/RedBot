package com.thatredhead.redbot.command;

public class CommandException extends Exception {

    public final String reason;

    public CommandException() {
        reason = "";
    }
    public CommandException(String reason) {
        this.reason = reason;
    }
}
