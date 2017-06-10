package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandArgumentException;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DnDCommands extends CommandGroup {

    public DnDCommands() {
        super("DnD Commands", "Collection of useful commands for RPGs",
                "dnd", Arrays.asList(new Roll()));
    }


    public static class Roll extends Command {

        private static final String pattern = "(?:\\s+)?([+-])?(?:\\s+)?(?:(?:(\\d+)?[dD](\\d+)(!)?(?:\\(([kK]|[dD])(\\d+)([lL]|[hH])\\))?)|(\\d+)(?![dD]))";

        public Roll() {
            super("roll", "Rolls a die in TTRPG fashion",
                    "<count>d<size>{! if exploding}{k/d<count>H/L} +/- <more dice, or modifiers>", PermissionContext.EVERYONE);
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
                    boolean explode = m.group(4) != null;
                    String kOrD = m.group(5);
                    String count = m.group(6);
                    String hOrL = m.group(7);
                    String mod = m.group(8);
                    if(sign == null) {
                        if(first) {
                            sign = "+";
                            first = false;
                        } else
                            throw new CommandException();
                    }

                    matched.append(sign).append(" ");

                    int sum = 0;
                    if(mod == null) {
                        int rollInt = rolls == null ? 1 : inRange(Integer.parseInt(rolls));
                        int sizeInt = inRange(Integer.parseInt(size));

                        if (rolls != null) matched.append(rolls);
                        matched.append("d").append(size);

                        if (explode) matched.append("!");

                        List<Integer> setRolls = new ArrayList<>();
                        List<Integer> ignored = new ArrayList<>();

                        for (int i = 0; i < rollInt; i++) {
                            int check, roll = 0;
                            do {
                                roll += (check = (int) (Math.random() * sizeInt + 1));
                            } while (explode && check == sizeInt);
                            setRolls.add(roll);
                        }

                        if ("k".equalsIgnoreCase(kOrD)) {
                            int countInt = Integer.parseInt(count);
                            boolean isHigh = "h".equalsIgnoreCase(hOrL);

                            List<Integer> keep = new ArrayList<>();

                            for (int c = 0; c < countInt && c < setRolls.size(); c++) {
                                int biggestIdx = 0;
                                for (int i = 0; i < setRolls.size(); i++) {
                                    if ((isHigh == setRolls.get(biggestIdx) < setRolls.get(i))
                                            && !keep.contains(i)) {
                                        biggestIdx = i;
                                    }
                                }
                                keep.add(biggestIdx);
                            }

                            for (int i = 0; i < setRolls.size(); i++) {
                                if (!keep.contains(i)) {
                                    ignored.add(i);
                                }
                            }

                            matched.append("(k").append(count).append(hOrL.toUpperCase()).append(")");
                        } else if ("d".equalsIgnoreCase(kOrD)) {

                            int countInt = Integer.parseInt(count);
                            boolean isHigh = "l".equalsIgnoreCase(hOrL);

                            for (int c = 0; c < countInt && c < setRolls.size(); c++) {
                                int biggestIdx = 0;
                                for (int i = 0; i < setRolls.size(); i++) {
                                    if ((isHigh == setRolls.get(biggestIdx) < setRolls.get(i))
                                            && !ignored.contains(i)) {
                                        biggestIdx = i;
                                    }
                                }
                                ignored.add(biggestIdx);
                            }

                            matched.append("(k").append(count).append(hOrL.toUpperCase()).append(")");
                        }

                        for(int i = 0; i < setRolls.size(); i++) {
                            if (ignored.contains(i)) {
                                result.append(", ~~").append(setRolls.get(i)).append("~~");
                            } else {
                                sum += setRolls.get(i);
                                result.append(", ").append(setRolls.get(i));
                            }
                        }
                    } else {
                        sum = Integer.parseInt(mod);

                        matched.append(mod);
                    }

                    matched.append(" ");

                    if("+".equals(sign)) total += sum;
                    else total -= sum;
                }
                if(matched.length() > 1018) throw new CommandArgumentException(1, matched.substring(0, 10) + "...", "Your roll request is waaay too long!");
                msgp.reply("Dice Roller", "", true,
                        "Input", "```\n" + matched.delete(0, 2).toString().trim() + "```",
                        "Output", result.length() < 100 ?
                                    result.indexOf(",", 1) > -1 ?
                                            "```\n" + result.delete(0, 2).toString() + " = " + total + "```" :
                                            "```\n" + total + "```" :
                                "```\nLOTS OF DICE = " + total + "```");
            } catch (NumberFormatException e) {
                throw new CommandException("Error parsing dice roll! Use `help` for proper format.");
            }
        }

        private int inRange(int val) {
            if(val > 0 && val <= 999) return val;
            throw new CommandException("Dice parameter out of range!");
        }
    }
}
