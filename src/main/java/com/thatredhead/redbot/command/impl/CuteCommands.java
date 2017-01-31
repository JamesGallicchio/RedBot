package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.command.ICommand;
import com.thatredhead.redbot.command.ICommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.data.DataHandler;
import sx.blah.discord.handle.obj.IChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CuteCommands implements ICommandGroup {

    private DataHandler datah;
    private List<ICommand> commands;
    private HashMap<IChannel, String> safeties;
    private List<String> engines;

    public CuteCommands(DataHandler datah) {
        this.datah = datah;
        safeties = datah.get("cutesafety", new TypeToken<HashMap<IChannel, String>>(){}.getType(), new HashMap<>());
        engines = datah.get("cuteengine", new TypeToken<List<String>>(){}.getType(), new ArrayList<>());
        commands = new ArrayList<>();
        commands.add(new CuteCommand());
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    public class CuteCommand implements ICommand {

        public CuteCommand() {

        }

        @Override
        public String getPermission() {
            return "cute.cute";
        }

        @Override
        public String getKeyword() {
            return "cute";
        }

        @Override
        public void invoke(MessageParser msgp) {
            if (!safeties.containsKey(msgp.getChannel()))
                safeties.put(msgp.getChannel(), "safe");

        }
    }
}