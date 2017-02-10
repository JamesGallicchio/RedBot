package com.thatredhead.redbot.command;

import com.thatredhead.redbot.permission.PermissionContext;

public interface ICommand {

    String getKeyword();
    String getDescription();
    String getUsage();
    String getPermission();
    PermissionContext getDefaultPermissions();
    void invoke(MessageParser msgp) throws CommandException;
}
