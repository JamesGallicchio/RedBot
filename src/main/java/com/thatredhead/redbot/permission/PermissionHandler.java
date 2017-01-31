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
        String[] permStructure = perm.split(".");
        for(int i = permStructure.length; i > 0; i--) {
            String check = Arrays.stream(permStructure).limit(i).collect(Collectors.joining("."));
            if(guildperms.containsKey(check))
                return guildperms.containsKey(perm) && guildperms.get(perm).hasPermission(user, channel);
        }
        return false;
    }

    public boolean hasPermission(String perm, IMessage message) {
        return hasPermission(perm, message.getAuthor(), message.getChannel());
    }

    public void add(IGuild g) {
        perms.put(g.getID(), new HashMap<>());
    }

    public void add(IGuild g, String perm) {
        if(!perms.containsKey(g.getID()))
            add(g);
        perms.get(g.getID()).put(perm, new PermissionContext());
    }

    public PermissionContext get(IGuild g, String perm) {
        return perms.get(g.getID()).get(perm);
    }

    public PermissionContext getOrAdd(IGuild g, String perm) {
        HashMap<String, PermissionContext> guildPerms = perms.get(g.getID());
        if(guildPerms == null)
            return perms.put(g.getID(), new HashMap<>()).put(perm, new PermissionContext());
        else
            if(perms.get(g).containsKey(perm))
                return perms.get(g).get(perm);
            else
                return perms.get(g).put(perm, new PermissionContext());
    }

    public PermissionContext remove(IGuild g, String perm) {
        return perms.get(g.getID()).remove(perm);
    }

    public String toStringForGuild(IGuild g) {
        StringBuilder sb = new StringBuilder();
        sb.append("Permissions for this guild: ```");
        for(Map.Entry<String, PermissionContext> p : perms.get(g).entrySet()) {
            sb.append("\n");
            sb.append(p.getKey());
            sb.append("\n");
            sb.append(p.getValue());
        }
        sb.append("```");
        return sb.toString();
    }
}