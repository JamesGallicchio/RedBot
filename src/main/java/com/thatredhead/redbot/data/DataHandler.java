package com.thatredhead.redbot.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.permission.PermissionContext;
import com.thatredhead.redbot.permission.PermissionContextSerializer;
import com.thatredhead.redbot.permission.PermissionHandler;
import com.thatredhead.redbot.permission.PermissionSerializer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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

        // Load perms
        permFile = Paths.get("data/perms.json");
        if(Files.exists(permFile)) {
            String json = "";
            try {
                json = readFromFile(permFile);
            } catch (FileNotFoundException e) {
                RedBot.reportError(e);
            }
            if(!json.isEmpty())
                permh = gson.fromJson(json, PermissionHandler.class);
            else
                permh = new PermissionHandler();
        }
        else
            permh = new PermissionHandler();
        savePerms();
    }

    /**
     * Gets the permission handler from this datahandler
     * @return PermissionHandler instance
     */
    public PermissionHandler getPermHandler() {
        return permh;
    }

    /**
     * Saves the permissions in memory to file
     */
    public void savePerms() {
        writeToFile(permFile, gson.toJson(permh));
    }

    /**
     * Saves an object in json form
     * @param obj the object to save
     * @param name the name to save it under
     * @return o, for chaining methods
     */
    public <T> T save(T obj, String name) {
        writeToFile(new File("data/" + name + ".json"), gson.toJson(obj));
        return obj;
    }

    /**
     * Gets an object from json
     * @param name the name the object is saved under
     * @param classOfT a class instance of T
     * @param <T> the type of the object to get
     * @return the object gotten from json
     * @throws FileNotFoundException
     */
    public <T> T get(String name, Class<T> classOfT) throws FileNotFoundException {
        return gson.fromJson(readFromFile("data/" + name + ".json"), classOfT);
    }

    /**
     * Gets an object from json
     * @param name the name the object is saved under
     * @param T the type of the object to get
     * @param <T> the type of the object to get
     * @return the object gotten from json
     * @throws FileNotFoundException
     */
    public <T> T get(String name, Type T) throws FileNotFoundException {
        return gson.fromJson(readFromFile("data/" + name + ".json"), T);
    }

    /**
     * Gets an object or defaults to another object if it doesn't exist
     * @param name the name of the object to get
     * @param classOfT a class instance of T
     * @param def the default object to use if name isn't present
     * @param <T> the type of object to get
     * @return the object gotten from json, or default
     */
    public <T> T get(String name, Class<T> classOfT, T def) {
        try {
            T obj = get(name, classOfT);
            return obj == null ? def : obj;
        } catch (FileNotFoundException e) {
            RedBot.reportError(e);
            return save(def, name);
        }
    }

    /**
     * Gets an object or defaults to another object if it doesn't exist
     * @param name the name of the object to get
     * @param T the Type of the object to get
     * @param def the default object to use if name isn't present
     * @param <T> the type of object to get
     * @return the object gotten from json, or default
     */
    public <T> T get(String name, Type T, T def) {
        try {
            T obj = get(name, T);
            return obj == null ? def : obj;
        } catch (FileNotFoundException e) {
            RedBot.reportError(e);
            return save(def, name);
        }
    }
    
    private static String readFromFile(String s) throws FileNotFoundException {
        return readFromFile(Paths.get(s));
    }

    private static String readFromFile(Path p) throws FileNotFoundException {
        try {
            return new String(Files.readAllBytes(p));
        } catch (NoSuchFileException e) {
            throw new FileNotFoundException();
        } catch (IOException e) {
            RedBot.reportError(e);
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
            RedBot.reportError(e);
        } finally {
            try {
                if(bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                RedBot.reportError(e);
            }
        }
    }
}
