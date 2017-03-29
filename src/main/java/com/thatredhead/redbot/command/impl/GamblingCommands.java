package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.econ.Account;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.util.Arrays;

public class GamblingCommands  extends CommandGroup {

    public GamblingCommands() {
        super("GamblingCommands", "Commands for playing a variety of gambling games",
                "gambling", Arrays.asList(
                    new SlotsCommand()
                ));
    }

    public static class SlotsCommand extends Command {

        public static final int ROWS = 3;
        public static final int COLS = 3;

        public static final double CHARGE = 10.0;

        public static final double REWARD_NORMAL = 100.0;
        public static final double REWARD_HIGH = 200.0;
        public static final double REWARD_JACKPOT = 1000.0;

        public static final double CHANCE_NORMAL = 0.2;
        public static final double CHANCE_HIGH = 0.05;
        public static final double CHANCE_JACKPOT = 0.01;

        public static final Emoji[] NORMAL_EMOJIS = getEmojiList("clown",
                "japanese_goblin", //
                "ghost",
                "robot_face", //
                "alien",
                "poop",
                "cat",
                "monkey",
                "santa",
                "motorcycle",
                "muscle", //
                "vulcan_salute", //
                "eyes",
                "heart",
                "bomb",
                "jeans",
                "purse",
                "athletic_shoe", //
                "rooster",
                "frog",
                "turtle",
                "whale",
                "dolphin",
                "octopus",
                "shark",
                "ant",
                "spider_web", //
                "rose",
                "deciduous_tree", //
                "cactus",
                "maple_leaf", //
                "grapes",
                "watermelon",
                "lemon",
                "banana",
                "pineapple",
                "pear",
                "peach",
                "cherries",
                "tomato",
                "avocado",
                "hot_pepper", //
                "mushroom",
                "croissant", //
                "cheese",
                "bacon",
                "pizza",
                "cookie",
                "anchor",
                "airplane",
                "crescent_moon", //
                "droplet",
                "basketball",
                "8ball", //
                "bell",
                "musical_note", //
                "musical_keyboard", //
                "telephone",
                "battery",
                "floppy_disk", //
                "film_frames", //
                "clapper", //
                "mag_right", //
                "bulb", //
                "books",
                "pencil",
                "pushpin",
                "scissors",
                "key",
                "hammer",
                "bow_and_arrow", //
                "dagger",
                "gear",
                "coffin",
                "crystal_ball", //
                "warning",
                "no_entry", //
                "radioactive",
                "question", //
                "checkered_flag");
        public static final Emoji[] HIGH_EMOJIS = getEmojiList("crown",
                "shamrock",
                "eggplant",
                "rainbow",
                "fire",
                "trophy",
                "moneybag",
                "heavy_dollar_sign",
                "sunny");
        public static final Emoji[] JACKPOT_EMOJIS = getEmojiList("gem");

        public SlotsCommand() {
            super("slots", "Take a spin on the slot machine!", PermissionContext.EVERYONE);
        }

        public void invoke(MessageParser msgp) {
            Account act = RedBot.getEconomy().getAccountForUser(msgp.getAuthor());
            if(act.getAmount() >= CHARGE) {

                act.subtract(CHARGE);

                double rando = Math.random();
                if(CHANCE_JACKPOT > rando) {
                    msgp.reply(getJackpotWin());
                    act.add(REWARD_JACKPOT);
                } else if(CHANCE_JACKPOT + CHANCE_HIGH > rando) {
                    msgp.reply(getHighWin());
                    act.add(REWARD_HIGH);
                } else if(CHANCE_JACKPOT + CHANCE_HIGH + CHANCE_NORMAL > rando) {
                    msgp.reply(getNormalWin());
                    act.add(REWARD_NORMAL);
                } else {
                    msgp.reply(getLose());
                }

            } else msgp.reply("You don't have enough money! (:diamonds:" + CHARGE +
                    ")\nYour balance: " + act.getAmount());
        }

        private static String getLose() {
            StringBuilder sb = new StringBuilder("__Slot Machine__\n");

            Emoji[] emojis = new Emoji[ROWS*COLS];

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    emojis[r*ROWS + c] = getRandomNotIn(emojis, NORMAL_EMOJIS, HIGH_EMOJIS, JACKPOT_EMOJIS);
                }
            }

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    sb.append(emojis[r*ROWS + c]).append(" ");
                }
                sb.append("\n");
            }

            sb.append("*Sorry! Better luck next time!*");

            return sb.toString();
        }

        private static String getNormalWin() {
            StringBuilder sb = new StringBuilder("__Slot Machine__\n");

            Emoji[] emojis = new Emoji[ROWS*COLS];

            for(int r = 0; r < ROWS; r++) {
                if(r == ROWS/2) {
                    Emoji winner = getRandomNotIn(emojis, NORMAL_EMOJIS);
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*ROWS + c] = winner;
                    }
                } else {
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*ROWS + c] = getRandomNotIn(emojis, NORMAL_EMOJIS, HIGH_EMOJIS, JACKPOT_EMOJIS);
                    }
                }
            }

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    sb.append(emojis[r*ROWS + c]).append(" ");
                }
                sb.append("\n");
            }

            sb.append("**You won :diamonds:" + REWARD_NORMAL + "!**");

            return sb.toString();
        }

        private static String getHighWin() {
            StringBuilder sb = new StringBuilder("__Slot Machine__\n");

            Emoji[] emojis = new Emoji[ROWS*COLS];

            for(int r = 0; r < ROWS; r++) {
                if(r == ROWS/2) {
                    Emoji winner = getRandomNotIn(emojis, HIGH_EMOJIS);
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*ROWS + c] = winner;
                    }
                } else {
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*ROWS + c] = getRandomNotIn(emojis, NORMAL_EMOJIS, HIGH_EMOJIS, JACKPOT_EMOJIS);
                    }
                }
            }

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    sb.append(emojis[r*ROWS + c]).append(" ");
                }
                sb.append("\n");
            }

            sb.append("**You won :diamonds:" + REWARD_HIGH + "!**");

            return sb.toString();
        }

        private static String getJackpotWin() {
            StringBuilder sb = new StringBuilder("__Slot Machine__\n");

            Emoji[] emojis = new Emoji[ROWS*COLS];

            for(int r = 0; r < ROWS; r++) {
                if(r == ROWS/2) {
                    Emoji winner = getRandomNotIn(emojis, JACKPOT_EMOJIS);
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*ROWS + c] = winner;
                    }
                } else {
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*ROWS + c] = getRandomNotIn(emojis, NORMAL_EMOJIS, HIGH_EMOJIS, JACKPOT_EMOJIS);
                    }
                }
            }

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    sb.append(emojis[r*ROWS + c]).append(" ");
                }
                sb.append("\n");
            }

            sb.append("**You won the jackpot: :diamonds:" + REWARD_JACKPOT + "!**");

            return sb.toString();
        }

        private static <T> T getRandomNotIn(T[] blacklist, T[]... options) {
            T possibility = getRandom(options);
            boolean contains = true;
            while(contains) {
                possibility = getRandom(options);
                contains = false;

                for(T one: blacklist)
                    if(one != null && one.equals(possibility)) {
                        contains = true;
                        break;
                    }
            }
            return possibility;
        }

        private static <T> T getRandom(T[]... options) {
            int length = Arrays.stream(options).mapToInt(it -> it.length).sum();

            int idx = (int) (Math.random() * length);

            for(T[] op: options) {
                if(idx < op.length)
                    return op[idx];
                idx -= op.length;
            }

            return null;
        }

        private static Emoji[] getEmojiList(String ... emojiAliases) {
            Emoji[] emojis = new Emoji[emojiAliases.length];

            for(int i = 0; i < emojiAliases.length; i++)
                emojis[i] = EmojiManager.getForAlias(emojiAliases[i]);

            return emojis;
        }
    }
}