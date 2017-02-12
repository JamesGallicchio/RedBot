package com.thatredhead.redbot.command;

import java.util.List;

public abstract class CommandGroup {

    protected String name;
    protected String description;
    protected String permission;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public abstract List<Command> getCommands();
}
