package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.command.CommandException;
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

        private static final String pattern = "([+-])?(?:(?:(\\d+)?[dD](\\d+))|(\\d+)(?![dD]))";

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
            return "<# of dice>d<size> +/- <more dice or modifiers>";
        }

        @Override
        public String getPermission() {
            return "dnd.roll";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getNobodyContext();
        }

        @Override
        public void invoke(MessageParser msgp) {
            try {
                StringBuilder matched = new StringBuilder();
                Matcher m = Pattern.compile(pattern).matcher(msgp.getContentAfter(1));
                int total = 0;
                while (m.find()) {
                    String sign = m.group(1);
                    String rolls = m.group(2);
                    String size = m.group(3);
                    String mod = m.group(4);
                    if(sign == null) sign = "+";
                    if(rolls == null) rolls = "1";

                    matched.append(sign).append(" ");

                    int sum = 0;
                    if(mod == null) {
                        int rollInt = inRange(Integer.parseInt(rolls));
                        int sizeInt = inRange(Integer.parseInt(size));

                        for(int i = 0; i < rollInt; i++)
                            sum += (int) (Math.random()*sizeInt+1);

                        matched.append(rolls).append("d").append(size);
                    } else {
                        sum = Integer.parseInt(mod);

                        matched.append(mod);
                    }

                    matched.append(" ");

                    if("+".equals(sign)) total += sum;
                    else total -= sum;
                }
                DiscordUtils.sendMessage("Result for `" + matched.delete(0, 2).toString().trim() + "`: **" + total + "**", msgp.getChannel());
            } catch (NumberFormatException e) {
                throw new CommandException("Error parsing dice roll! Use help for proper format.");
            }

            /*
            Matcher m = Pattern.compile(pattern).matcher(msgp.getContentAfter(1));
            try {
                if (m.find()) {

                    int dice, size, mod;

                    if (m.group(1) == null) dice = 1;
                    else {
                        if (m.group(1).length() > 3) throw new CommandException("Number of dice is too big! (Max 999)");
                        dice = Integer.parseInt(m.group(1));
                    }

                    if (m.group(2).length() > 3) throw new CommandException("Size of dice is too big! (Max 999)");
                    size = Integer.parseInt(m.group(2));

                    if (m.group(3) == null) mod = 0;
                    else {
                        if (m.group(3).length() > 3) throw new CommandException("Modifier is too big! (Max 999)");
                        mod = Integer.parseInt(m.group(3));
                    }

                    int total = mod;
                    for (int i = 0; i < dice; i++)
                        total += (int) (Math.random() * size) + 1;

                    DiscordUtils.sendMessage("Result for `" + dice + "d" + size + " + " + mod + "`: **" + total + "**", msgp.getChannel());
                } else throw new NumberFormatException("Bad format- use: (# of dice)d(dice size) + offset");
            } catch (NumberFormatException e) {
                throw new CommandException("Unable to parse numbers");
            }*/
        }

        private int inRange(int val) {
            if(val > 0 && val <= 999) return val;
            throw new CommandException("Dice parameter out of range!");
        }
    }
}
