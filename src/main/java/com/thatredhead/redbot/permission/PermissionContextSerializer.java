package com.thatredhead.redbot.permission;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PermissionContextSerializer implements JsonSerializer<PermissionContext>, JsonDeserializer<PermissionContext> {

    @Override
    public JsonElement serialize(PermissionContext permissionContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject o = new JsonObject();
        o.addProperty("id", permissionContext.id);
        o.addProperty("negate", permissionContext.negate);
        o.addProperty("op", permissionContext.operation == PermissionContext.Operation.AND);

        JsonArray sub = new JsonArray();
        for(PermissionContext p : permissionContext.list)
            sub.add(serialize(p, type, jsonSerializationContext));

        o.add("list", sub);
        return o;
    }

    @Override
    public PermissionContext deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        PermissionContext perm = new PermissionContext();
        JsonObject o = jsonElement.getAsJsonObject();

        perm.id = o.get("id").getAsString();
        perm.negate = o.get("negate").getAsBoolean();
        perm.operation = o.get("op").getAsBoolean() ? PermissionContext.Operation.AND : PermissionContext.Operation.OR;

        ArrayList<PermissionContext> list = new ArrayList<>();
        for(JsonElement e : o.getAsJsonArray("list"))
            list.add(deserialize(e, type, jsonDeserializationContext));
        perm.list = list;
        return perm;
    }
}
