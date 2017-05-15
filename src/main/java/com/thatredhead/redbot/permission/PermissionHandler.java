package com.thatredhead.redbot.permission;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionHandler {

    // Permission -> (GuildID/ChannelID -> Permission)
    private Map<String, Map<GuildOrChannel, PermissionContext>> perms;

    public PermissionHandler() {
        this(new HashMap<>());
    }

    public PermissionHandler(Map<String, Map<GuildOrChannel, PermissionContext>> perms) {
        this.perms = perms;
    }

    public boolean hasPermission(Command cmd, IMessage message) {
        return hasPermission(cmd, message.getAuthor(), message.getChannel());
    }

    public boolean hasPermission(Command cmd, IUser user, IChannel channel) {
        return hasPermission(cmd.getPermission(), user, channel, cmd.isEnabledByDefault(), cmd.getDefaultPermissions());
    }

    /**
     * Check if a user has a certain permission in a specified channel
     * @param perm permission to check for
     * @param user user to check if has permission
     * @param channel channel in which to check if user has permission
     * @param defaultPerms PermissionContext to put if that perm is not specified
     * @return true if user has permission, false otherwise
     */
    public boolean hasPermission(String perm, IUser user, IChannel channel, boolean enabledByDefault, PermissionContext defaultPerms) {

        // If the perm is an empty string that always equates to true
        if ("".equals(perm)) return true;

        if(channel.isPrivate()) {
            return defaultPerms.applies(user, channel);
        }

        // If perms doesn't contain this perm, put a new hashmap in
        if (!perms.containsKey(perm)) {
            perms.put(perm, new HashMap<>());
            save();
        }

        // Get the perm's permissions map
        Map<GuildOrChannel, PermissionContext> permissions = perms.get(perm);

        // Return if the channel's perm applies
        PermissionContext channelPerm = permissions.get(channel);
        if (channelPerm != null)
            return channelPerm.applies(user, channel);


        // Return if the guild's perm applies
        PermissionContext guildPerm = permissions.get(channel.getGuild());
        if(guildPerm != null)
            if (guildPerm == PermissionContext.NULL)
                return defaultPerms.applies(user, channel);
            else
                return guildPerm.applies(user, channel);

        // If enabled by default, add it/return if it applies
        if(enabledByDefault) {
            permissions.put(new GuildOrChannel(channel.getGuild()), defaultPerms);
            save();
            return defaultPerms.applies(user, channel);
        }

        if(perm.contains(".")) {
            Map<GuildOrChannel, PermissionContext> groupPerms = perms.get(perm.split(".")[0]);

            // Return if the channel's group perm applies
            PermissionContext channelGroupPerm = groupPerms.get(channel);
            if (channelGroupPerm != null)
                return channelGroupPerm.applies(user, channel);


            // Return if the guild's group perm applies
            PermissionContext guildGroupPerm = groupPerms.get(channel.getGuild());
            if (guildGroupPerm == PermissionContext.NULL)
                return defaultPerms.applies(user, channel);
        }

        return false;
    }

    public boolean isEnabledFor(String perm, IChannel c) {

        Map<GuildOrChannel, PermissionContext> list = perms.get(perm);

        return list != null && list.get(c) != null || isEnabledFor(perm, c.getGuild());
    }

    public boolean isEnabledFor(String perm, IGuild g) {

        Map<GuildOrChannel, PermissionContext> list = perms.get(perm);

        return list != null && list.get(g) != null;
    }

    public Map<String, PermissionContext> getPermissionsFor(IChannel c) {
        Map<String, PermissionContext> channelPerms = new HashMap<>();

        for(Map.Entry<String, Map<GuildOrChannel, PermissionContext>> entry : perms.entrySet()) {
            Map<GuildOrChannel, PermissionContext> map = entry.getValue();

            PermissionContext perm = map.get(c);
            if(perm != null)
                channelPerms.put(entry.getKey(), perm);
            else {
                perm = map.get(c.getGuild());
                if(perm != null)
                    channelPerms.put(entry.getKey(), perm);
            }
        }

        return channelPerms;
    }

    public Map<String, PermissionContext> getPermissionsFor(IGuild g) {
        return perms.entrySet().stream()
                .filter(it -> it.getValue().containsKey(g))
                .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().get(g)));
    }

    public boolean add(String perm, IGuild g, PermissionContext context) {
        return add(perm, new GuildOrChannel(g), context);
    }

    public boolean add(String perm, IChannel c, PermissionContext context) {
        return add(perm, new GuildOrChannel(c), context);
    }

    public boolean add(String perm, GuildOrChannel obj, PermissionContext context) {
        perms.putIfAbsent(perm, new HashMap<>());

        if (context == null)
            context = PermissionContext.NULL;

        Map<GuildOrChannel, PermissionContext> map = perms.get(perm);
        if(map.containsKey(obj)) {
            map.put(obj, context);
            save();
            return true;
        }
        map.put(obj, context);
        save();
        return false;
    }

    public Map<String, Map<GuildOrChannel, PermissionContext>> getPermissions() {
        return perms;
    }

    /**
     * Saves these perms to a file
     */
    public void save() {
        RedBot.getDataHandler().save(this, RedBot.PERM_FILE_NAME);
    }
}

