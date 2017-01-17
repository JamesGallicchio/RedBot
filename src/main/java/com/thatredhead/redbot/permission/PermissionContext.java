package com.thatredhead.redbot.permission;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

public class PermissionContext {

    private String id;
    private IDiscordClient client;
    private List<PermissionContext> list;

    public PermissionContext(List<PermissionContext> perms) {
        list = perms;
    }

    public PermissionContext(IDiscordObject o, List<PermissionContext> blacklist) {
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

    public List<PermissionContext> getSubPerms() {
        return list;
    }

    public void setSubPerms(List<PermissionContext> list) {
        this.list = list;
    }

    public IDiscordObject getDiscordObject() {
        if(client == null) return null;
        return chooseNotNull(
                client.getUserByID(id),
                client.getChannelByID(id),
                client.getRoleByID(id),
                client.getGuildByID(id)
        );
    }

    private IDiscordObject chooseNotNull(IDiscordObject... o) {
        for (IDiscordObject anO : o) if (anO != null) return anO;
        return null;
    }
}
