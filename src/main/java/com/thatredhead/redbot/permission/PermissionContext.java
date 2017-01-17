package com.thatredhead.redbot.permission;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;

public class PermissionContext {

    private String id;
    private IDiscordClient client;
    private ArrayList<PermissionContext> list;

    public PermissionContext(ArrayList<PermissionContext> perms) {
        list = perms;
    }

    public PermissionContext(IDiscordObject o, ArrayList<PermissionContext> blacklist) {
        id = o.getID();
        client = o.getClient();
        list = blacklist;
    }

    public PermissionContext(IDiscordObject o) {
        this(o, null);
    }

    public boolean hasPermission(IUser user, IChannel channel) {
        if(id == null)
            return list.stream().anyMatch(it -> it.hasPermission(user, channel));
        return (id.equals(user.getID()) ||
                id.equals(channel.getID()) ||
                id.equals(channel.getGuild().getID()) ||
                user.getRolesForGuild(channel.getGuild())
                        .stream().anyMatch(it -> id.equals(it.getID()))) &&
                list.stream().noneMatch(it -> it.hasPermission(user, channel));
    }

    public ArrayList<PermissionContext> getSubPerms() {
        return list;
    }

    public void setSubPerms(ArrayList<PermissionContext> list) {
        this.list = list;
    }

    public IDiscordObject getDiscordObject() {
        if(client == null) return null;
        return firstNotNull(
                client.getUserByID(id),
                client.getChannelByID(id),
                client.getRoleByID(id),
                client.getGuildByID(id)
        );
    }

    private IDiscordObject firstNotNull(IDiscordObject... o) {
        for (IDiscordObject anO : o) if (anO != null) return anO;
        return null;
    }
}
