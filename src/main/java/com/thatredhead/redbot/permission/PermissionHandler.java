package com.thatredhead.redbot.permission;

import com.thatredhead.redbot.data.DataHandler;
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
    DataHandler datah;

    public PermissionHandler(DataHandler datah) {
        this(new HashMap<>(), datah);
    }

    public PermissionHandler(HashMap<String, HashMap<String, PermissionContext>> perms, DataHandler datah) {
        this.perms = perms;
        this.datah = datah;
    }

    public boolean hasPermission(String perm, IUser user, IChannel channel) {
        if(!perms.containsKey(channel.getGuild().getID()))
            perms.put(channel.getGuild().getID(), new HashMap<>());

        HashMap<String, PermissionContext> guildperms = perms.get(channel.getGuild().getID());
        if("".equals(perm)) return true;
        String[] permStructure = perm.split("\\.");
        for(int i = permStructure.length; i > 0; i--) {
            String check = Arrays.stream(permStructure).limit(i).collect(Collectors.joining("."));
            if(guildperms.containsKey(check))
                return guildperms.get(check).hasPermission(user, channel);
        }
        return false;
    }

    public boolean hasPermission(String perm, IUser user, IChannel channel, PermissionContext defaultPerms) {
        if(!perms.containsKey(channel.getGuild().getID()))
            perms.put(channel.getGuild().getID(), new HashMap<>());

        HashMap<String, PermissionContext> guildperms = perms.get(channel.getGuild().getID());
        if("".equals(perm)) return true;
        String[] permStructure = perm.split("\\.");
        for(int i = permStructure.length; i > 0; i--) {
            String check = Arrays.stream(permStructure).limit(i).collect(Collectors.joining("."));
            if(guildperms.containsKey(check))
                return guildperms.get(check).hasPermission(user, channel);
        }
        perms.get(channel.getGuild().getID()).put(perm, defaultPerms);
        return defaultPerms.hasPermission(user, channel);
    }

    public boolean hasPermission(String perm, IMessage message) {
        return hasPermission(perm, message.getAuthor(), message.getChannel());
    }

    public boolean hasPermission(String perm, IMessage message, PermissionContext defaultPerms) {
        return hasPermission(perm, message.getAuthor(), message.getChannel(), defaultPerms);
    }

    public void add(IGuild g) {
        perms.put(g.getID(), new HashMap<>());
    }

    public void add(IGuild g, String perm) {
        if(!perms.containsKey(g.getID()))
            add(g);
        perms.get(g.getID()).put(perm, PermissionContext.getNobodyContext());
    }

    public PermissionContext get(IGuild g, String perm) {
        return perms.get(g.getID()).get(perm);
    }

    public HashMap<String, PermissionContext> getOrAdd(IGuild g) {
        HashMap<String, PermissionContext> guildPerms = perms.get(g.getID());
        if(guildPerms == null)
            return perms.put(g.getID(), new HashMap<>());
        else
            return guildPerms;
    }

    public PermissionContext getOrAdd(IGuild g, String perm) {
        HashMap<String, PermissionContext> guildPerms = perms.get(g.getID());
        if(guildPerms == null)
            return perms.put(g.getID(), new HashMap<>()).put(perm, PermissionContext.getNobodyContext());
        else
            if(guildPerms.containsKey(perm))
                return guildPerms.get(perm);
            else {
                PermissionContext blank = PermissionContext.getNobodyContext();
                guildPerms.put(perm, blank);
                return blank;
            }
    }

    public PermissionContext remove(IGuild g, String perm) {
        return perms.get(g.getID()).remove(perm);
    }

    public String toStringForGuild(IGuild g) {
        StringBuilder sb = new StringBuilder();
        sb.append("Permissions for this guild: ```");
        for(Map.Entry<String, PermissionContext> p : this.getOrAdd(g).entrySet()) {
            sb.append("\n");
            sb.append(p.getKey());
            sb.append("\n");
            sb.append(p.getValue().toString());
        }
        sb.append("```");
        return sb.toString();
    }

    public void save() {
        datah.savePerms();
    }
}