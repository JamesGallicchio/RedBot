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
    private boolean negate;
    private Operation operation;

    public PermissionContext(ArrayList<PermissionContext> sub) {
        this(null, sub);
    }

    public PermissionContext(IDiscordObject o) {
        this(o, null);
    }

    public PermissionContext(IDiscordObject o, ArrayList<PermissionContext> sub) {
        this(o, sub, Operation.AND, false);
    }

    public PermissionContext(IDiscordObject o, ArrayList<PermissionContext> sub, Operation op, boolean negate) {
        if(o != null) {
            id = o.getID();
            client = o.getClient();
        }
        list = sub;
        this.operation = op;
        this.negate = negate;
    }

    public boolean hasPermission(IUser user, IChannel channel) {
        if(operation == Operation.AND) {
            boolean val = negate == list.stream().allMatch(it -> it.hasPermission(user, channel));
            if(id == null) return val;
            return applies(user, channel) && val;
        } else {
            boolean val = negate == list.stream().anyMatch(it -> it.hasPermission(user, channel));
            if(id == null) return val;
            return applies(user, channel) && val;
        }
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

    private boolean applies(IUser user, IChannel channel) {
        return id.equals(user.getID()) ||
               id.equals(channel.getID()) ||
               id.equals(channel.getGuild().getID()) ||
               user.getRolesForGuild(channel.getGuild())
                        .stream().anyMatch(it -> id.equals(it.getID()));
    }

    private IDiscordObject firstNotNull(IDiscordObject... o) {
        for (IDiscordObject anO : o) if (anO != null) return anO;
        return null;
    }

    public enum Operation {
        AND,
        OR;
    }
}