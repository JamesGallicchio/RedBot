package com.thatredhead.redbot.data;

import com.google.gson.Gson;
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
                permh = new PermissionHandler(gson.fromJson(new FileReader(permFile), HashMap.class), this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        else {
            permh = new PermissionHandler(this);
            savePerms();
        }
    }

    public PermissionHandler getPermHandler() throws FileNotFoundException {
        return permh;
    }

    public void savePerms() {
        try{
            gson.toJson(permh.getPerms(), new FileWriter(permFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(Object o, String name) {
        try{
            gson.toJson(o, new FileWriter("data/" + name + ".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T get(String name, Class<T> typeClass) throws FileNotFoundException {
        return gson.fromJson(new FileReader("data/" + name + ".json"), typeClass);
    }
}
