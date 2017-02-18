package com.thatredhead.redbot.command;

public class CommandException extends RuntimeException {

    public CommandException() {
        super("Exception running command!");
    }
    public CommandException(String reason) {
        super(reason);
    }
}
