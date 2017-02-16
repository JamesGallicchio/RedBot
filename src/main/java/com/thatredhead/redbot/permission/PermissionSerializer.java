package com.thatredhead.redbot.permission;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON Serializer for PermissionHandler class
 */
public class PermissionSerializer implements JsonSerializer<PermissionHandler>, JsonDeserializer<PermissionHandler> {

    /*
    Example json:
    {
        "guildID": {
            "perm.example": {Permission Context Json Object},
            "example.perm": {PCJO},
            "system": {PCJO}
        }, "otherGuildID": { ... }
    }
     */

    @Override
    public JsonObject serialize(PermissionHandler perms, Type typeOfSrc,
                                JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        // For each guild in the perms list
        for(Map.Entry<String, HashMap<String, PermissionContext>> guild : perms.perms.entrySet()) {
            JsonObject obj = new JsonObject();
            // For each permission in the guild
            for(Map.Entry<String, PermissionContext> permission : guild.getValue().entrySet())
                // Add the perm to json as "perm": <permissioncontext>
                obj.add(permission.getKey(), context.serialize(permission.getValue()));
            // Add the guild to json as "guild ID": {perms as object}
            json.add(guild.getKey(), obj);
        }

        return json;
    }

    @Override
    public PermissionHandler deserialize(JsonElement json, Type type,
                                         JsonDeserializationContext context) throws JsonParseException {
        JsonObject o = json.getAsJsonObject();
        HashMap<String, HashMap<String, PermissionContext>> guilds = new HashMap<>();

        // For each {"guildID" -> perm list object}
        for(Map.Entry<String, JsonElement> guild : o.entrySet()) {
            HashMap<String, PermissionContext> perms = new HashMap<>();
            // For each {"perm" -> permission context object}
            for(Map.Entry<String, JsonElement> perm : guild.getValue().getAsJsonObject().entrySet())
                // Add permission to map as "perm" -> permission context obj
                perms.put(perm.getKey(), context.deserialize(perm.getValue(), PermissionContext.class));
            // Add permission map to guild map as "guildID" -> permission map
            guilds.put(guild.getKey(), perms);
        }

        return new PermissionHandler(guilds);
    }
}
