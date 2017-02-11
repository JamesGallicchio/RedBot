package com.thatredhead.redbot.command;

public class CommandException extends RuntimeException {

    public CommandException() {
        super();
    }
    public CommandException(String reason) {
        super(reason);
    }
}
