package com.thatredhead.redbot.permission;

import com.thatredhead.redbot.data.DataHandler;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class PermissionHandler {

    private HashMap<String, PermissionContext> perms;
    private DataHandler datah;

    public PermissionHandler(DataHandler datah) {
        this(new HashMap<>(), datah);
    }

    public PermissionHandler(HashMap<String, PermissionContext> perms, DataHandler datah) {
        this.perms = perms;
        this.datah = datah;
    }

    public HashMap<String, PermissionContext> getPerms() {
        return perms;
    }

    public boolean hasPermission(String perm, IUser user, IChannel channel) {
        String[] permStructure = perm.split(".");
        for(int i = permStructure.length; i > 0; i++) {
            String check = Arrays.stream(permStructure).limit(i).collect(Collectors.joining("."));
            if(perms.containsKey(check))
                return perms.containsKey(perm) && perms.get(perm).hasPermission(user, channel);
        }
        return false;
    }

    public boolean hasPermission(String perm, IMessage message) {
        return hasPermission(perm, message.getAuthor(), message.getChannel());
    }
}