package com.thatredhead.redbot.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.econ.Economy;
import com.thatredhead.redbot.permission.PermissionHandler;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataHandler {

    public final static Path PERM_FILE = Paths.get("data/perms.json");
    public final static Path ECON_FILE = Paths.get("data/econ.json");

    private Gson gson = new GsonBuilder()
                            .enableComplexMapKeySerialization()
                            .create();
    private PermissionHandler permh;
    private Economy econ;

    public DataHandler() {

        // Load perms
        if(Files.exists(PERM_FILE)) {
            String json = null;
            try {
                json = readFromFile(PERM_FILE);
            } catch (FileNotFoundException e) {
                RedBot.reportError(e);
            }
            if(json != null && !json.isEmpty())
                permh = gson.fromJson(json, PermissionHandler.class);
            else
                permh = new PermissionHandler();
        }
        else
            permh = new PermissionHandler();
        save(permh, PERM_FILE);

        if(Files.exists(ECON_FILE)) {
            String json = null;
            try {
                json = readFromFile(ECON_FILE);
            } catch (FileNotFoundException e) {
                RedBot.reportError(e);
            }
            if(json != null && !json.isEmpty())
                econ = gson.fromJson(json, Economy.class);
            else
                econ = new Economy();
        }
        else
            econ = new Economy();
        save(econ, ECON_FILE);
    }

    /**
     * Gets the permission handler from this datahandler
     * @return PermissionHandler instance
     */
    public PermissionHandler getPermHandler() {
        return permh;
    }

    public Economy getEconomy() {
        return econ;
    }

    /**
     * Saves an object in json form
     * @param obj the object to save
     * @param name the name to save it under
     * @return o, for chaining methods
     */
    public <T> T save(T obj, String name) {
        return save(obj, Paths.get("data/" + name + ".json"));
    }

    /**
     * Saves an object in json form
     * @param obj the object to save
     * @param path the name to save it under
     * @return o, for chaining methods
     */
    public <T> T save(T obj, Path path) {
        writeToFile(path, gson.toJson(obj));
        return obj;
    }

    /**
     * Gets an object from json
     * @param name the name the object is saved under
     * @param classOfT a class instance of T
     * @param <T> the type of the object to get
     * @return the object gotten from json
     */
    public <T> T get(String name, Class<T> classOfT) {
        try {
            return gson.fromJson(readFromFile("data/" + name + ".json"), classOfT);
        } catch (FileNotFoundException ignored) {

        } return null;
    }

    /**
     * Gets an object from json
     * @param name the name the object is saved under
     * @param T the type of the object to get
     * @param <T> the type of the object to get
     * @return the object gotten from json
     */
    public <T> T get(String name, Type T) {
        try {
            return gson.fromJson(readFromFile("data/" + name + ".json"), T);
        } catch (FileNotFoundException ignored) {

        } return null;
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
        T obj = get(name, classOfT);
        return obj == null ? save(def, name) : obj;
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
        T obj = get(name, T);
        return obj == null ? save(def, name) : obj;
    }

    /**
     * Reads a file into a string
     * @param s name of the file to get
     * @return contents of the file in a String
     * @throws FileNotFoundException if the file can't be found
     */
    private static String readFromFile(String s) throws FileNotFoundException {
        return readFromFile(Paths.get(s));
    }

    /**
     * Reads a file into a String
     * @param p the Path to the file
     * @return the contents of the file as a String
     * @throws FileNotFoundException if the file can't be found
     */
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

    /**
     * Write a String to a file
     * @param f the Path to the file to write the string to
     * @param s the String to write to the file
     */
    private static void writeToFile(Path f, String s) {
        writeToFile(f.toFile(), s);
    }

    /**
     * Write a String to a file
     * @param f the file to write the string to
     * @param s the string to write to the file
     */
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
