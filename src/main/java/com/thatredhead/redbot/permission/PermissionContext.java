package com.thatredhead.redbot.permission;

import sx.blah.discord.handle.obj.*;

import java.util.ArrayList;

public class PermissionContext {

    IDiscordObject obj;
    Permissions perm;
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
        obj = o;
        list = sub;
        this.operation = op;
        this.negate = negate;
    }

    public PermissionContext(Permissions p) {
        this();
        perm = p;
    }

    public PermissionContext(Permissions p, ArrayList<PermissionContext> sub, Operation op, boolean negate) {
        perm = p;
        list = sub;
        this.operation = op;
        this.negate = negate;
    }

    public boolean hasPermission(IUser user, IChannel channel) {
        if(list == null || list.isEmpty()) return applies(user, channel);
        if(operation == Operation.AND) {
            boolean val = negate != list.stream().allMatch(it -> it.hasPermission(user, channel));
            if(obj == null) return val;
            return applies(user, channel) && val;
        } else {
            boolean val = negate != list.stream().anyMatch(it -> it.hasPermission(user, channel));
            if(obj == null) return val;
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

    public IDiscordObject getDiscordObject() {
        return obj;
    }

    private boolean applies(IUser user, IChannel channel) {
        return obj == null ?
                   perm == null || channel.getModifiedPermissions(user).contains(perm)
               : user.equals(obj) ||
                   channel.equals(obj) ||
                   user.getRolesForGuild(channel.getGuild())
                        .stream().anyMatch(it -> it.equals(obj));
    }

    private String getName() {
        if(obj == null) return perm.toString();
        if(obj instanceof IUser) return ((IUser) obj).getName();
        if(obj instanceof IRole) return ((IRole) obj).getName();
        if(obj instanceof IChannel) return ((IChannel) obj).getName();
        return "";
    }

    public enum Operation {
        AND,
        OR
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getName());
        if(list != null) {
            for (PermissionContext pc : list) {
                result.append('\n');
                result.append(pc.toString());
            }
            if(result.charAt(0) == '\n') result.setCharAt(0, '\t');
        }
        return result.toString().replaceAll("\n", "\n\t");
    }
}