package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
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
                    "<# of dice>d<size> +/- <more dice, or modifiers>", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) {
            try {
                StringBuilder matched = new StringBuilder();
                Matcher m = Pattern.compile(pattern).matcher(msgp.getContentAfter(1));

                int total = 0;
                StringBuilder result = new StringBuilder();

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

                        for(int i = 0; i < rollInt; i++) {
                            int roll = (int) (Math.random() * sizeInt + 1);
                            sum += roll;
                            result.append(" + ").append(roll);
                        }

                        matched.append(rolls).append("d").append(size);
                    } else {
                        sum = Integer.parseInt(mod);

                        matched.append(mod);
                    }

                    matched.append(" ");

                    if("+".equals(sign)) total += sum;
                    else total -= sum;
                }
                Utilities4D4J.sendMessage("`" + matched.delete(0, 2).toString().trim() + "`: *" +
                        (result.length() < 100 && result.indexOf("+") > -1 ? result.delete(0, 3).toString() + "* = **" + total + "**" : "*" + total + "**"),
                        msgp.getChannel());
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
