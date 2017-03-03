package com.thatredhead.redbot.command;

import java.util.List;

public abstract class CommandGroup {

    protected String name;
    protected String description;
    protected String permission;
    protected List<Command> commands;

    public CommandGroup(String name, String description, String permission, List<Command> commands) {
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public List<Command> getCommands() {
        return commands;
    }
}
