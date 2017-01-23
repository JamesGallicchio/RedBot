package com.thatredhead.redbot.command;

import com.thatredhead.redbot.data.DataHandler;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CuteCommands implements ICommandGroup {

    private DataHandler datah;
    private List<ICommand> commands;
    private HashMap<IChannel, String> safeties;

    public CuteCommands(DataHandler datah) {
        this.datah = datah;
        safeties = datah.get("cutesafety", HashMap.class, new HashMap());
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
        public void invoke(String msg, IUser user, IChannel channel) {
            if (!safeties.containsKey(channel))
                safeties.put(channel, "safe");
        }
    }
}
