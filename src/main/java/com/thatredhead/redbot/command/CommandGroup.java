package com.thatredhead.redbot.command;

import java.util.List;

public abstract class CommandGroup {

    protected String name;
    protected String description;
    protected String permission;
    protected List<Command> commands;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public  List<Command> getCommands() {
        return commands;
    }
}
