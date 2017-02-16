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
        o.addProperty("i", permissionContext.obj == null ? null : permissionContext.obj.getID());
        o.addProperty("p", permissionContext.perm == null ? null : permissionContext.perm.ordinal());
        o.addProperty("e", permissionContext.isEveryone);
        o.addProperty("t", getType(permissionContext.obj));
        o.addProperty("n", permissionContext.negate);
        o.addProperty("o", permissionContext.operation == PermissionContext.Operation.AND);

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

        JsonElement id = o.get("i");
        JsonElement perms = o.get("p");
        JsonElement everyone = o.get("e");
        JsonElement negate = o.get("n");
        JsonElement operation = o.get("o");
        pc.obj = id == null || id.isJsonNull() ? null : getObject(id.getAsString(), o.get("type").getAsInt());
        pc.perm = perms == null || perms.isJsonNull() ? null : Permissions.valueOf(perms.getAsString());
        pc.isEveryone = !(everyone == null || everyone.isJsonNull()) && everyone.getAsBoolean();
        pc.negate = !(negate == null || negate.isJsonNull()) && negate.getAsBoolean();
        pc.operation = !(operation == null || operation.isJsonNull()) && operation.getAsBoolean() ? PermissionContext.Operation.AND : PermissionContext.Operation.OR;

        ArrayList<PermissionContext> list = new ArrayList<>();
        JsonArray jsonList = o.getAsJsonArray("list");
        if(jsonList != null && !jsonList.isJsonNull())
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
