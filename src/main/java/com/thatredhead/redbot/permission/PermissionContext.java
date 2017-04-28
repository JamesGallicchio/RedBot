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
            case NOBODY:
                return false;
            case EVERYONE:
                return true;
            case ADMIN:
                if(channel.getModifiedPermissions(user).contains(Permissions.ADMINISTRATOR)) return true;
            case OWNER:
                if(user.getLongID() == channel.getGuild().getOwnerLongID()) return true;
            case BOT_OWNER:
                if(RedBot.OWNER_ID == user.getLongID()) return true;
            default:
                return false;
        }
    }
}