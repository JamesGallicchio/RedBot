package com.thatredhead.redbot.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thatredhead.redbot.permission.PermissionContext;
import com.thatredhead.redbot.permission.PermissionContextSerializer;
import com.thatredhead.redbot.permission.PermissionHandler;
import com.thatredhead.redbot.permission.PermissionSerializer;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;

public class DataHandler {

    Gson gson;
    PermissionHandler permh;
    File permFile;

    public DataHandler() {

        gson = new GsonBuilder()
                .registerTypeAdapter(PermissionContext.class, new PermissionContextSerializer())
                .registerTypeAdapter(PermissionHandler.class, new PermissionSerializer(this))
                .create();

        permFile = new File("data/perms.json");
        if(permFile.exists())
            try {
                HashMap<String, HashMap<String, PermissionContext>> perms = gson.fromJson(new FileReader(permFile), HashMap.class);
                if(perms == null)
                    permh = new PermissionHandler(this);
                else
                    permh = new PermissionHandler(perms, this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        else {
            permh = new PermissionHandler(this);
            savePerms();
        }
    }

    public PermissionHandler getPermHandler() {
        return permh;
    }

    public void savePerms() {
        try{
            gson.toJson(permh, new FileWriter(permFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object save(Object o, String name) {
        try{
            gson.toJson(o, new FileWriter("data/" + name + ".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o;
    }

    public <T> T get(String name, Class<T> classOfT) throws FileNotFoundException {
        return gson.fromJson(new FileReader("data/" + name + ".json"), classOfT);
    }

    public <T> T get(String name, Type T) throws FileNotFoundException {
        return gson.fromJson(new FileReader("data/" + name + ".json"), T);
    }

    public <T> T get(String name, Class<T> classOfT, T def) {
        try {
            T obj = get(name, classOfT);
            return obj == null ? def : obj;
        } catch (FileNotFoundException e) {
            return (T) save(def, name);
        }
    }

    public <T> T get(String name, Type T, T def) {
        try {
            T obj = get(name, T);
            return obj == null ? def : obj;
        } catch (FileNotFoundException e) {
            return (T) save(def, name);
        }
    }
}
