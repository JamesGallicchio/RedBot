package com.thatredhead.redbot.command;

public class CommandArgumentException extends CommandException {

    public final int idx;
    public final String arg;
    public final String correctFormat;

    public CommandArgumentException(int idx, String arg, String correctFormat) {
        this.idx = idx;
        this.arg = arg;
        this.correctFormat = correctFormat;
    }
}
