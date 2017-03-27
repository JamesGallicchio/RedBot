package com.thatredhead.redbot.permission;

import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

public enum PermissionContext {
    BOT_OWNER,
    OWNER,
    ADMIN,
    MOD,
    EVERYONE,
    NOBODY,
    NULL;

    public boolean applies(IUser user, IChannel channel) {
        switch(this) {
            case BOT_OWNER:
                return RedBot.OWNER_ID.equals(user.getID());
            case ADMIN:
                return channel.getModifiedPermissions(user).contains(Permissions.ADMINISTRATOR);
            case EVERYONE:
                return true;
            case NOBODY:
                return false;
            default:
                return false;
        }
    }
}