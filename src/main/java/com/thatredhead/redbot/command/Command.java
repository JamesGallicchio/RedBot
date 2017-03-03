package com.thatredhead.redbot.command;

import com.thatredhead.redbot.permission.PermissionContext;

public abstract class Command {

    protected CommandGroup group;
    protected String keyword;
    protected String description;
    protected String usage;
    protected boolean noPrefix;
    protected String permission;

    public Command(String keyword, String description) {
        this(keyword, description, keyword, keyword, true);
    }

    public Command(String keyword, String description, String usage) {
        this(keyword, description, usage, keyword, true);
    }

    public Command(String keyword, String description, String usage, String permission, boolean usesPrefix) {
        this.keyword = keyword;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.noPrefix = !usesPrefix;
    }

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
        return !noPrefix;
    }

    public String getPermission() {
        return group == null ? permission : group.getPermission() + "." + permission;
    }

    public abstract PermissionContext getDefaultPermissions();
    public abstract void invoke(MessageParser msgp) throws CommandException;
}
