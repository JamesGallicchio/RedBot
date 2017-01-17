package com.thatredhead.redbot.permission;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionHandler {

    Map<String, PermissionContext> perms;

    public PermissionHandler() {
        perms = new HashMap<>();
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
}