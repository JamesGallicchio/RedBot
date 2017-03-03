package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DnDCommands extends CommandGroup {

    public DnDCommands() {
        super("DnD Commands", "Collection of useful commands for RPGs",
                "dnd", Arrays.asList(new Roll()));
    }


    public static class Roll extends Command {

        private static final String pattern = "(?:\\s+)?([+-])?(?:\\s+)?(?:(?:(\\d+)?[dD](\\d+))|(\\d+)(?![dD]))";

        public Roll() {
            super("roll", "Rolls a die in TTRPG fashion",
                    "<# of dice>d<size> +/- <more dice, or modifiers>");
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
                boolean first = true;
                while (m.find()) {
                    String sign = m.group(1);
                    String rolls = m.group(2);
                    String size = m.group(3);
                    String mod = m.group(4);
                    if(sign == null) {
                        if(first) {
                            sign = "+";
                            first = false;
                        } else
                            throw new CommandException();
                    }
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
        }

        private int inRange(int val) {
            if(val > 0 && val <= 999) return val;
            throw new CommandException("Dice parameter out of range!");
        }
    }
}
