package com.thatredhead.redbot.permission;

import sx.blah.discord.handle.obj.*;

import java.util.ArrayList;

public class PermissionContext {

    IDiscordObject obj;
    Permissions perm;
    boolean isEveryone;
    ArrayList<PermissionContext> list;
    boolean negate;
    Operation operation;

    PermissionContext() {
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

    public static PermissionContext getEveryoneContext() {
        PermissionContext perm = new PermissionContext();
        perm.isEveryone = true;
        return perm;
    }

    public static PermissionContext getNobodyContext() {
        return new PermissionContext();
    }

    /**
     * Checks if user has the permission described by this context in channel
     * @param user the user to compare permissions for
     * @param channel the channel in which permissions should be checked
     * @return whether or not this permission context applies
     */
    public boolean hasPermission(IUser user, IChannel channel) {
        // If no subs, it'll just be whether this applies
        if(list == null || list.isEmpty()) return applies(user, channel);

        // If AND, use allMatch
        if(operation == Operation.AND) {

            // negate != blah just means if negate is true it'll make true -> false or false -> true
            boolean val = negate != list.stream().allMatch(it -> it.hasPermission(user, channel));

            // return whether this context applies AND whether all the subs are correct
            return applies(user, channel) && val;

        } else { // If OR, use anyMatch

            boolean val = negate != list.stream().anyMatch(it -> it.hasPermission(user, channel));

            return applies(user, channel) && val;
        }
    }

    public ArrayList<PermissionContext> getSubPerms() {
        return list;
    }

    /**
     * Adds a context as a sub to this context
     * @param perm the permission context to add
     */
    public void addSub(PermissionContext perm) {
        list.add(perm);
    }

    /**
     * Removes a context as a sub to this context
     * @param perm the permission context to remove
     */
    public void removeSub(PermissionContext perm) {
        list.remove(perm);
    }

    /**
     * Gets the discord object this context corresponds to, or null if it doesn't correspond to one
     * @return the discord object for this context
     */
    public IDiscordObject getDiscordObject() {
        return obj;
    }

    /**
     * Gets the permission this context corresponds to, or null
     * @return the permission for this context
     */
    public Permissions getPermission() {
        return perm;
    }

    /**
     * Gets whether or not this context corresponds to everyone in the guild
     * @return whether or not this context corresponds to everyone in the guild
     */
    public boolean isEveryone() {
        return isEveryone;
    }

    /**
     * Checks if the discord object, permission, or everyone applies
     * @param user the user to check in
     * @param channel the channel to check in
     * @return whether or not this context applies
     */
    private boolean applies(IUser user, IChannel channel) {
        return obj == null ?
                   perm == null ?
                           isEveryone : // obj and perm null, return everyone or nobody
                           channel.getModifiedPermissions(user).contains(perm) // obj null, return perm
               : user.equals(obj) || // compare obj to user, channel, or roles, cuz it can be any of those
                   channel.equals(obj) ||
                   user.getRolesForGuild(channel.getGuild())
                        .stream().anyMatch(it -> it.equals(obj));
    }

    /**
     * Gets the mention for this discord object or a string representation of the permission or everyone
     * @return the mention/string representation of this permission context's descriptor
     */
    private String getMention() {
        if(obj == null)
            return perm == null ?
                    isEveryone ? "EVERYONE" : "NOBODY" // obj and perm are null, return everyone or nobody
                    : perm.toString(); // obj null, return perm's name
        // return obj's mention
        if(obj instanceof IUser) return ((IUser) obj).mention();
        if(obj instanceof IRole) return ((IRole) obj).mention();
        if(obj instanceof IChannel) return ((IChannel) obj).mention();
        return "";
    }

    /**
     * Gets the name for this discord object or a string representation of the permission or everyone
     * @return the name/string representation of this permission context's descriptor
     */
    private String getName() {
        if(obj == null)
            return perm == null ?
                    isEveryone ? "EVERYONE" : "NOBODY"
                    : perm.toString();
        if(obj instanceof IUser) return ((IUser) obj).getName();
        if(obj instanceof IRole) return ((IRole) obj).getName();
        if(obj instanceof IChannel) return ((IChannel) obj).getName();
        return "";
    }

    /**
     * Enum describing whether the operation for the sub list should be and or or
     */
    public enum Operation {
        AND,
        OR
    }

    /**
     * Gives a string representation of this permission context and its sub
     * @return string representation of this permission context
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getMention()); // append this context's mention

        // append the subs
        if(list != null) {
            result.append("\n\tAND ").append(
                    // append the comparison operation
                    operation.equals(Operation.AND) ?
                        negate ? "NOT ALL" : "ALL" :
                        negate ? "NONE" : "ONE")
                    .append(" OF THESE:");
            // append each sub indented
            for (PermissionContext pc : list) {
                result.append("\n");
                result.append(indent(pc.toString()));
            }
        }

        return result.toString();
    }

    private static String indent(String s) {
        return "\t" + s.replace("\n", "\n\t");
    }
}