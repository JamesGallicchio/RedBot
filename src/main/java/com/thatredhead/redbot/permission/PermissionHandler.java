package com.thatredhead.redbot.permission;

import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionHandler {

    HashMap<String, HashMap<String, PermissionContext>> perms;

    public PermissionHandler() {
        this(new HashMap<>());
    }

    public PermissionHandler(HashMap<String, HashMap<String, PermissionContext>> perms) {
        this.perms = perms;
    }

    /**
     * Check if a user has a certain permission in a specified channel
     * @param perm permission to check for
     * @param user user to check if has permission
     * @param channel channel in which to check if user has permission
     * @return true if user has permission, false otherwise
     */
    public boolean hasPermission(String perm, IUser user, IChannel channel) {
        return hasPermission(perm, user, channel, null);
    }

    /**
     * Check if a user has a certain permission in a specified channel
     * @param perm permission to check for
     * @param user user to check if has permission
     * @param channel channel in which to check if user has permission
     * @param defaultPerms PermissionContext to put if that perm is not specified
     * @return true if user has permission, false otherwise
     */
    public boolean hasPermission(String perm, IUser user, IChannel channel, PermissionContext defaultPerms) {
        // If perms doesn't contain this guild, put a new hashmap in
        if(!perms.containsKey(channel.getGuild().getID()))
            perms.put(channel.getGuild().getID(), new HashMap<>());

        // Get the guild's permissions map
        HashMap<String, PermissionContext> guildperms = perms.get(channel.getGuild().getID());

        // If the perm is an empty string that always equates to true
        if("".equals(perm)) return true;

        // Split the perm based on . because if user has
        // PERM while PERM.example isn't specified, then PERM.example will still return true
        String[] permStructure = perm.split("\\.");

        // Iterate backwards from the entire perm to perm without the last term .. etc
        for(int i = permStructure.length; i > 0; i--) {
            // Collect perm array as first.second.third to i
            String check = Arrays.stream(permStructure).limit(i).collect(Collectors.joining("."));
            // If this guild has this perm, return true, otherwise continue
            if(guildperms.containsKey(check))
                return guildperms.get(check).hasPermission(user, channel);
        }

        // If defaultPerms is null return false
        if(defaultPerms != null) {
            // Add defaultPerms to guild perm list and check if user has permission in this default perm
            perms.get(channel.getGuild().getID()).put(perm, defaultPerms);
            return defaultPerms.hasPermission(user, channel);
        }
        return false;
    }

    /**
     * Check if the message's author has perm in the message's channel
     * @param perm the perm to check
     * @param message the message to check for perm
     * @return whether or not the user has perm
     */
    public boolean hasPermission(String perm, IMessage message) {
        return hasPermission(perm, message, null);
    }

    /**
     * Check if the message's author has perm in the message's channel, or use the default
     * @param perm the perm to check
     * @param message the message to check for perm
     * @param defaultPerms the default perm to check if the perm isn't specified
     * @return whether or not the user has perm
     */
    public boolean hasPermission(String perm, IMessage message, PermissionContext defaultPerms) {
        return hasPermission(perm, message.getAuthor(), message.getChannel(), defaultPerms);
    }

    /**
     * Add guild to the guild list if it isn't already in there
     * @param g the guild to add
     */
    public void add(IGuild g) {
        if(!perms.containsKey(g.getID()))
            perms.put(g.getID(), new HashMap<>());
    }

    /**
     * Add permission in guild
     * @param g guild to add perm to
     * @param perm the perm to add
     */
    public void add(IGuild g, String perm) {
        get(g).put(perm, PermissionContext.getNobodyContext());
    }

    /**
     * Get the permissions for the guild or add the guild if it hasn't been added yet
     * @param g the guild to get or add permissions for
     * @return the map of permissions for the specified guild
     */
    public HashMap<String, PermissionContext> get(IGuild g) {
        HashMap<String, PermissionContext> guildPerms = perms.get(g.getID());
        if(guildPerms == null)
            return perms.put(g.getID(), new HashMap<>());
        else
            return guildPerms;
    }

    /**
     * Gets a permission or adds an empty one
     * @param g the guild to get the permission for
     * @param perm the perm to get from that guild
     * @return the PermissionContext for specified perm
     */
    public PermissionContext get(IGuild g, String perm) {
        // Get the perms for that guild
        HashMap<String, PermissionContext> guildPerms = perms.get(g.getID());

        // If the guild contains the perm get it, otherwise add a new one
        if(guildPerms.containsKey(perm))
            return guildPerms.get(perm);
        else {
            PermissionContext blank = PermissionContext.getNobodyContext();
            guildPerms.put(perm, blank);
            return blank;
        }
    }

    /**
     * Removes the specified perm from the specified guild
     * @param g the guild to remove the perm from
     * @param perm the perm to remove
     * @return the perm that was removed
     */
    public PermissionContext remove(IGuild g, String perm) {
        return get(g).remove(perm);
    }

    /**
     * Makes a string version of the permissions for a specified guild
     * @param g the guild to get permissions for
     * @return a string representation of the permissions for a specified guild
     */
    public String toStringForGuild(IGuild g) {
        StringBuilder sb = new StringBuilder();

        sb.append("Permissions for this guild: ```");

        // For each permission in the guild
        for(Map.Entry<String, PermissionContext> p : get(g).entrySet()) {
            // Append \nPERM\n\tCONTEXT
            sb.append('\n').append(p.getKey()).append('\n');
            sb.append(indent(p.getValue().toString()));
        }
        sb.append("```");
        return sb.toString();
    }

    private static String indent(String s) {
        return "\t" + s.replace("\n", "\n\t");
    }

    /**
     * Saves these perms to a file
     */
    public void save() {
        RedBot.getDataHandler().savePerms();
    }
}