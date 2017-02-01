package com.thatredhead.redbot.permission;

import com.google.gson.*;
import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PermissionContextSerializer implements JsonSerializer<PermissionContext>, JsonDeserializer<PermissionContext> {

    @Override
    public JsonElement serialize(PermissionContext permissionContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject o = new JsonObject();
        o.addProperty("id", permissionContext.obj == null ? "" : permissionContext.obj.getID());
        o.addProperty("type", getType(permissionContext.obj));
        o.addProperty("negate", permissionContext.negate);
        o.addProperty("op", permissionContext.operation == PermissionContext.Operation.AND);

        JsonArray sub = new JsonArray();
        if(permissionContext.list != null)
            for(PermissionContext p : permissionContext.list)
                sub.add(serialize(p, type, jsonSerializationContext));

        o.add("list", sub);
        return o;
    }

    @Override
    public PermissionContext deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        PermissionContext perm = new PermissionContext();
        JsonObject o = jsonElement.getAsJsonObject();

        String id = o.get("id").getAsString();
        perm.obj = "".equals(id) ? null : getObject(id, o.get("type").getAsInt());
        perm.negate = o.get("negate").getAsBoolean();
        perm.operation = o.get("op").getAsBoolean() ? PermissionContext.Operation.AND : PermissionContext.Operation.OR;

        ArrayList<PermissionContext> list = new ArrayList<>();
        for(JsonElement e : o.getAsJsonArray("list"))
            list.add(deserialize(e, type, jsonDeserializationContext));
        perm.list = list;
        return perm;
    }

    private static int getType(IDiscordObject o) {
        if(o instanceof IUser) return 0;
        if(o instanceof IRole) return 1;
        if(o instanceof IChannel) return 2;
        return -1;
    }

    private static IDiscordObject getObject(String id, int type) {
        switch(type) {
            case 0:
                return RedBot.client.getUserByID(id);
            case 1:
                return RedBot.client.getRoleByID(id);
            case 2:
                return RedBot.client.getChannelByID(id);
            default:
                return null;
        }
    }
}
