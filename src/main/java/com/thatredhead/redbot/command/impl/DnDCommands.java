package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.command.ICommand;
import com.thatredhead.redbot.command.ICommandGroup;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DnDCommands implements ICommandGroup {

    private List<ICommand> commands;

    public DnDCommands() {
        commands = new ArrayList<>();

        commands.add(new Roll());
    }

    @Override
    public List<ICommand> getCommands() {
        return commands;
    }


    public static class Roll implements ICommand {

        private static final String pattern = "(\\d+)?d(\\d+)(?:\\+(\\d+))?";

        @Override
        public String getKeyword() {
            return "roll";
        }

        @Override
        public String getPermission() {
            return "dnd.roll";
        }

        @Override
        public void invoke(String msg, IUser user, IChannel channel) {
            Matcher m = Pattern.compile(pattern).matcher(msg);
            if(m.find()) {

                int dice, size, mod;

                if(m.group(1) == null) dice = 1;
                else dice = Integer.parseInt(m.group(3));

                size = Integer.parseInt(m.group(2));

                if(m.group(3) == null) mod = 0;
                else mod = Integer.parseInt(m.group(3));

                int total = mod;
                for(int i = 0; i < dice; i++)
                    total += (int) (Math.random()*size) + 1;

                DiscordUtils.sendMessage("Result for " + dice + "d" + size + " + " + mod + ": " + total, channel);
            } else DiscordUtils.sendMessage("Bad format- use: (# of dice)d(dice size) + offset", channel);
        }
    }
}
