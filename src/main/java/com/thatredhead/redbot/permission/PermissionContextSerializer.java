package com.thatredhead.redbot.permission;

import com.google.gson.*;
import com.thatredhead.redbot.RedBot;
import sx.blah.discord.handle.obj.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PermissionContextSerializer implements JsonSerializer<PermissionContext>, JsonDeserializer<PermissionContext> {

    @Override
    public JsonElement serialize(PermissionContext permissionContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject o = new JsonObject();

        // add this context's properties
        o.addProperty("id", permissionContext.obj == null ? null : permissionContext.obj.getID());
        o.addProperty("perm", permissionContext.perm == null ? null : permissionContext.perm.ordinal());
        o.addProperty("everyone", permissionContext.isEveryone);
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
        PermissionContext pc = new PermissionContext();
        JsonObject o = jsonElement.getAsJsonObject();

        JsonElement id = o.get("id");
        JsonElement perms = o.get("perm");
        pc.obj = id.isJsonNull() ? null : getObject(id.getAsString(), o.get("type").getAsInt());
        pc.perm = perms.isJsonNull() ? null : Permissions.valueOf(perms.getAsString());
        pc.isEveryone = o.get("everyone").getAsBoolean();
        pc.negate = o.get("negate").getAsBoolean();
        pc.operation = o.get("op").getAsBoolean() ? PermissionContext.Operation.AND : PermissionContext.Operation.OR;

        ArrayList<PermissionContext> list = new ArrayList<>();
        for(JsonElement e : o.getAsJsonArray("list"))
            list.add(deserialize(e, type, jsonDeserializationContext));
        pc.list = list;

        return pc;
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
                return RedBot.getClient().getUserByID(id);
            case 1:
                return RedBot.getClient().getRoleByID(id);
            case 2:
                return RedBot.getClient().getChannelByID(id);
            default:
                return null;
        }
    }
}
