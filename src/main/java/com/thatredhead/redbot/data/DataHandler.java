package com.thatredhead.redbot.data;

import com.google.gson.Gson;
import com.thatredhead.redbot.permission.PermissionContext;
import com.thatredhead.redbot.permission.PermissionHandler;

import java.io.*;
import java.util.HashMap;

public class DataHandler {

    Gson gson;
    PermissionHandler permh;
    File permFile;

    public DataHandler() {

        gson = new Gson();

        permFile = new File("data/perms.json");
        if(permFile.exists())
            try {
                HashMap<String, PermissionContext> perms = gson.fromJson(new FileReader(permFile), HashMap.class);
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
            gson.toJson(permh.getPerms(), new FileWriter(permFile));
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

    public <T> T get(String name, Class<T> typeClass) throws FileNotFoundException {
        return gson.fromJson(new FileReader("data/" + name + ".json"), typeClass);
    }

    public <T> T get(String name, Class<T> typeClass, T def) {
        try {
            T obj = get(name, typeClass);
            return obj == null ? def : obj;
        } catch (FileNotFoundException e) {
            return (T) save(def, name);
        }
    }
}
