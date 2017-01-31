package com.thatredhead.redbot.permission;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.*;

import java.util.ArrayList;

public class PermissionContext {

    String id;

    ArrayList<PermissionContext> list;
    boolean negate;
    Operation operation;

    public PermissionContext() {
        this(new ArrayList<>());
    }

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

    public void addSub(PermissionContext perm) {
        list.add(perm);
    }

    public void removeSub(PermissionContext perm) {
        list.remove(perm);
    }

    public IDiscordObject getDiscordObject(IDiscordClient client) {
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

    private String getName(IDiscordClient client) {
        IDiscordObject o = getDiscordObject(client);
        if(o instanceof IUser) return ((User) o).getName();
        if(o instanceof IGuild) return ((Guild) o).getName();
        if(o instanceof IRole) return ((IRole) o).getName();
        if(o instanceof IChannel) return ((IChannel) o).getName();
        return "";
    }

    private IDiscordObject firstNotNull(IDiscordObject... o) {
        for (IDiscordObject anO : o) if (anO != null) return anO;
        return null;
    }

    public enum Operation {
        AND,
        OR
    }

    public String toString(IDiscordClient client) {
        StringBuilder result = new StringBuilder();
        result.append(getName(client));
        for(PermissionContext pc : list)
            result.append(pc.toString(client));
        for(int pos = 0; pos < result.length(); pos++) {
            pos = result.indexOf("\n", pos) + 1;
            result.insert(pos, "\t");
        }
        return result.toString();
    }
}