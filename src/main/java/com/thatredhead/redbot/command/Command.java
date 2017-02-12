package com.thatredhead.redbot.command;

import com.thatredhead.redbot.permission.PermissionContext;

public abstract class Command {

    protected CommandGroup group;
    protected String keyword;
    protected String description;
    protected String usage;
    protected boolean usesPrefix;
    protected String permission;

    public String getKeyword() {
        return keyword;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public boolean usesPrefix() {
        return usesPrefix;
    }

    public String getPermission() {
        return group == null ? permission : group.getPermission() + "." + permission;
    }

    public abstract PermissionContext getDefaultPermissions();
    public abstract void invoke(MessageParser msgp) throws CommandException;
}
