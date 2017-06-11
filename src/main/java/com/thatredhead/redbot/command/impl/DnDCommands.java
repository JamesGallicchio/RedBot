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

        private static final String pattern = "([+-])?(?:(?:(\\d+)\\*)?(\\d+)?d(\\d+)(!)?(?:\\(([kKrR])(\\d+)([hHlL])\\))?|(\\d+))";

        public Roll() {
            super("roll", "Rolls a die in TTRPG fashion",
                    "<count>d<size>{! if exploding}{k/r<count>H/L} +/- <more dice, or modifiers>", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) {
            try {
                String matched = msgp.getContentAfter(1)
                        .replace(" ", "").replace("\t", "")
                        .replace("\n", "");

                Matcher m = Pattern.compile(pattern).matcher(matched);

                int total = 0;
                StringBuilder result = new StringBuilder();

                int idx = 0;
                while (m.find()) {
                    if (idx != m.start()) throw new CommandException("Unknown symbol: `" + matched.substring(0, idx) + "` **" + matched.charAt(idx) + "** `" + matched.substring(idx+1) + "`");

                    String sign = m.group(1);
                    String mult = m.group(2);
                    String rolls = m.group(3);
                    String size = m.group(4);
                    boolean explode = m.group(5) != null;
                    String kOrR = m.group(6);
                    String count = m.group(7);
                    String hOrL = m.group(8);
                    String mod = m.group(9);
                    if(sign == null) {
                        if(idx == 0) {
                            sign = "+";
                        } else
                            throw new CommandException("Missing sign on term `" + m.group(0) + "`");
                    }

                    int sum = 0;
                    if(mod == null) {
                        int multInt = mult == null ? 1 : inRange(Integer.parseInt(mult));
                        int rollInt = rolls == null ? 1 : inRange(Integer.parseInt(rolls));
                        int sizeInt = inRange(Integer.parseInt(size));

                        List<Integer> setRolls = new ArrayList<>();
                        List<Integer> ignored = new ArrayList<>();

                        for (int i = 0; i < rollInt; i++) {
                            int check, roll = 0;
                            do {
                                roll += (check = (int) (Math.random() * sizeInt) + 1);
                            } while (explode && check != 1 && check == sizeInt);
                            setRolls.add(multInt * roll);
                        }

                        boolean keep = "k".equalsIgnoreCase(kOrR);
                        boolean rem = "r".equalsIgnoreCase(kOrR);
                        if (keep || rem) {
                            int countInt = Integer.parseInt(count);
                            boolean isHigh = "h".equalsIgnoreCase(hOrL);

                            if (keep) {
                                countInt = rollInt - countInt;
                                isHigh = !isHigh;
                            }

                            // Remove countInt of the isHigh? highest : lowest dice
                            for (int c = 0; c < countInt && c < setRolls.size(); c++) {
                                int biggestIdx = 0;
                                while(ignored.contains(biggestIdx)) biggestIdx++;
                                for (int i = 0; i < setRolls.size(); i++) {
                                    if ((isHigh == setRolls.get(biggestIdx) < setRolls.get(i))
                                            && !ignored.contains(i)) {
                                        biggestIdx = i;
                                    }
                                }
                                ignored.add(biggestIdx);
                            }

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
                    }

                    if("+".equals(sign)) total += sum;
                    else total -= sum;
                    idx = m.end();
                }
                if (idx != matched.length()) throw new CommandException("Unknown symbol: `" + matched.substring(0, idx) + "` **" + matched.charAt(idx) + "** `" + matched.substring(idx+1) + "`");

                if(matched.length() > 1018) throw new CommandArgumentException(1, matched.substring(0, 10) + "...", "Your roll request is waaay too long!");
                msgp.reply("Dice Roller", "", true,
                        "Input", "```\n" + matched.replace("+", " + ").replace("-", " - ") + "```",
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
