package com.thatredhead.redbot.data;

import com.google.gson.Gson;
import com.thatredhead.redbot.permission.PermissionHandler;

import java.io.File;
import java.io.FileReader;

public class DataHandler {

    public DataHandler() {
        //Not much to do here >.>
    }

    public PermissionHandler getPermHandler() {
        File permFile = new File("data/perms.json");
        if(permFile.exists()) {
            return new Gson().fromJson(new FileReader(permFile), )
        }
    }
}
