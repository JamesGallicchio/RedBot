package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.command.ICommand;
import com.thatredhead.redbot.command.ICommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

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
        public String getDescription() {
            return "Rolls a die in D&D fashion";
        }

        @Override
        public String getUsage() {
            return "<# of dice>d<size> + <modifier>";
        }

        @Override
        public String getPermission() {
            return "dnd.roll";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return new PermissionContext();
        }

        @Override
        public void invoke(MessageParser msgp) {
            Matcher m = Pattern.compile(pattern).matcher(msgp.getContentAfter(1));
            try {
                if (m.find()) {

                    int dice, size, mod;

                    if (m.group(1) == null) dice = 1;
                    else {
                        if (m.group(1).length() > 3) throw new NumberFormatException("Number of dice is too big! (Max 999)");
                        dice = Integer.parseInt(m.group(1));
                    }

                    if (m.group(2).length() > 3) throw new NumberFormatException("Size of dice is too big! (Max 999)");
                    size = Integer.parseInt(m.group(2));

                    if (m.group(3) == null) mod = 0;
                    else {
                        if (m.group(3).length() > 3) throw new NumberFormatException("Modifier is too big! (Max 999)");
                        mod = Integer.parseInt(m.group(3));
                    }

                    int total = mod;
                    for (int i = 0; i < dice; i++)
                        total += (int) (Math.random() * size) + 1;

                    DiscordUtils.sendMessage("Result for `" + dice + "d" + size + " + " + mod + "`: **" + total + "**", msgp.getChannel());
                } else throw new NumberFormatException("Bad format- use: (# of dice)d(dice size) + offset");
            } catch (NumberFormatException e) {
                DiscordUtils.sendTemporaryMessage(e.getMessage(), msgp.getChannel());
            }
        }
    }
}
