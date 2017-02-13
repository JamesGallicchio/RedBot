package com.thatredhead.redbot.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thatredhead.redbot.permission.PermissionContext;
import com.thatredhead.redbot.permission.PermissionContextSerializer;
import com.thatredhead.redbot.permission.PermissionHandler;
import com.thatredhead.redbot.permission.PermissionSerializer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataHandler {

    private Gson gson;
    private PermissionHandler permh;
    private Path permFile;

    public DataHandler() {

        gson = new GsonBuilder()
                .registerTypeAdapter(PermissionContext.class, new PermissionContextSerializer())
                .registerTypeAdapter(PermissionHandler.class, new PermissionSerializer())
                .create();

        permFile = Paths.get("data/perms.json");
        if(Files.exists(permFile)) {
            String json = readFromFile(permFile);
            if(!json.isEmpty())
                permh = gson.fromJson(json, PermissionHandler.class);
            else
                permh = new PermissionHandler();
        }
        else
            permh = new PermissionHandler();
        savePerms();
    }

    public PermissionHandler getPermHandler() {
        return permh;
    }

    public void savePerms() {
        writeToFile(permFile, gson.toJson(permh));
    }

    public Object save(Object o, String name) {
        writeToFile(new File("data/" + name + ".json"), gson.toJson(o));
        return o;
    }

    public <T> T get(String name, Class<T> classOfT) throws FileNotFoundException {
        return gson.fromJson(readFromFile("data/" + name + ".json"), classOfT);
    }

    public <T> T get(String name, Type T) throws FileNotFoundException {
        return gson.fromJson(readFromFile("data/" + name + ".json"), T);
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

    private static String readFromFile(String s) {
        return readFromFile(Paths.get(s));
    }

    private static String readFromFile(Path p) {
        try {
            return new String(Files.readAllBytes(p));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeToFile(Path f, String s) {
        writeToFile(f.toFile(), s);
    }

    private static void writeToFile(File f, String s) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(f);
            bw = new BufferedWriter(fw);
            bw.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
