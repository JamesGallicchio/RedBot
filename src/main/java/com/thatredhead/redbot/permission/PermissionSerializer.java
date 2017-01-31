package com.thatredhead.redbot.permission;

import com.google.gson.*;
import com.thatredhead.redbot.data.DataHandler;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PermissionSerializer implements JsonSerializer<PermissionHandler>, JsonDeserializer<PermissionHandler> {

    private DataHandler datah;

    public PermissionSerializer(DataHandler datah) {
        this.datah = datah;
    }

    @Override
    public JsonObject serialize(PermissionHandler perms, Type typeOfSrc,
                                JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        for(Map.Entry<String, HashMap<String, PermissionContext>> guild : perms.perms.entrySet()) {
            JsonObject obj = new JsonObject();

            for(Map.Entry<String, PermissionContext> permission : guild.getValue().entrySet())
                obj.add(permission.getKey(), context.serialize(permission.getValue()));

            json.add(guild.getKey(), obj);
        }

        return json;
    }

    @Override
    public PermissionHandler deserialize(JsonElement json, Type type,
                                         JsonDeserializationContext context) throws JsonParseException {
        JsonObject o = json.getAsJsonObject();
        HashMap<String, HashMap<String, PermissionContext>> guilds = new HashMap<>();

        for(Map.Entry<String, JsonElement> guild : o.entrySet()) {
            HashMap<String, PermissionContext> perms = new HashMap<>();

            for(Map.Entry<String, JsonElement> perm : guild.getValue().getAsJsonObject().entrySet())
                perms.put(perm.getKey(), context.deserialize(perm.getValue(), PermissionContext.class));

            guilds.put(guild.getKey(), perms);
        }

        return new PermissionHandler(guilds, datah);
    }
}
