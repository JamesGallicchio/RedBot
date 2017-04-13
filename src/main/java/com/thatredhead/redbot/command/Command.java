package com.thatredhead.redbot.command;

import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

public abstract class Command {

    protected CommandGroup group;
    protected String keyword;
    protected String description;
    protected String usage;
    protected boolean noPrefix;
    protected boolean enabledByDefault;
    protected String permission;
    protected PermissionContext defaultPermission;

    public Command(String keyword, String description, PermissionContext defaultPerm) {
        this(keyword, description, keyword, keyword, true, false, defaultPerm);
    }

    public Command(String keyword, String description, boolean enabledByDefault, PermissionContext defaultPerm) {
        this(keyword, description, keyword, keyword, true, enabledByDefault, defaultPerm);
    }

    public Command(String keyword, String description, String usage, PermissionContext defaultPerm) {
        this(keyword, description, usage, keyword, true, false, defaultPerm);
    }

    public Command(String keyword, String description, String usage, boolean enabledByDefault, PermissionContext defaultPerm) {
        this(keyword, description, usage, keyword, true, enabledByDefault, defaultPerm);
    }

    public Command(String keyword, String description, String usage, String permission, boolean usesPrefix, boolean enabledByDefault, PermissionContext defaultPerm) {
        this.keyword = keyword;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.noPrefix = !usesPrefix;
        this.enabledByDefault = enabledByDefault;
        this.defaultPermission = defaultPerm;
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

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public PermissionContext getDefaultPermissions() {
        return defaultPermission;
    }

    public abstract void invoke(MessageParser msgp) throws CommandException;

    public static Command of(String keyword, String description, String usage, String permission, boolean usesPrefix, boolean enabledByDefault, PermissionContext defaultPerm, CommandImpl.CommandInvoker onInvoke) {
        return new CommandImpl(keyword, description, usage, permission, usesPrefix, enabledByDefault, defaultPerm, onInvoke);
    }
}

class CommandImpl extends Command {

    private CommandInvoker invoker;

    public CommandImpl(String keyword, String description, String usage, String permission, boolean usesPrefix, boolean enabledByDefault, PermissionContext defaultPerm, CommandInvoker onInvoke) {
        super(keyword, description, usage, permission, usesPrefix, enabledByDefault, defaultPerm);
        invoker = onInvoke;
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {
        invoker.invoke(msgp);
    }

    @FunctionalInterface
    public interface CommandInvoker {
        void invoke(MessageParser msgp);
    }
}