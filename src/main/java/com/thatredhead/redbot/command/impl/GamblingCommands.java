package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.econ.Account;
import com.thatredhead.redbot.econ.Economy;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.api.internal.json.objects.EmbedObject;

import java.util.Arrays;

public class GamblingCommands  extends CommandGroup {

    public GamblingCommands() {
        super("GamblingCommands", "Commands for playing a variety of gambling games",
                "gambling", Arrays.asList(
                        new SlotsCommand(),
                        new Magic8Ball()
                ));
    }

    public static class Magic8Ball extends Command {

        public static String[] responses = {
                "It is certain", "It is decidedly so",
                "Without a doubt", "Yes definitely",
                "You may rely on it", "As I see it, yes",
                "Most likely", "Outlook good",
                "Yes", "Signs point to yes",
                "Reply hazy try again", "Ask again later",
                "Better not tell you now", "Cannot predict now",
                "Concentrate and ask again", "Don't count on it",
                "My reply is no", "My sources say no",
                "Outlook not so good", "Very doubtful"
        };

        public Magic8Ball() {
            super("magic8ball", "Shakes a magic 8 ball", PermissionContext.EVERYONE);
        }

        public void invoke(MessageParser msgp) {
            msgp.reply(Utilities4D4J.makeEmbed("Magic 8 Ball says...", responses[(int) (responses.length*Math.random())], true));
        }
    }

    public static class SlotsCommand extends Command {

        public static final int ROWS = 3;
        public static final int COLS = 5;

        public static final double CHARGE = 50.0;

        public static final double REWARD_NORMAL = 100.0;
        public static final double REWARD_HIGH = 200.0;
        public static final double REWARD_JACKPOT = 1000.0;

        public static final double CHANCE_NORMAL = 0.25;
        public static final double CHANCE_HIGH = 0.06;
        public static final double CHANCE_JACKPOT = 0.01;

        public static final Emoji[] NORMAL_EMOJIS = getEmojiList("clown",
                "japanese_goblin",
                "ghost",
                "robot_face",
                "alien",
                "poop",
                "cat",
                "monkey",
                "santa",
                "motorcycle",
                "muscle",
                "vulcan_salute",
                "eyes",
                "heart",
                "bomb",
                "jeans",
                "purse",
                "athletic_shoe",
                "rooster",
                "frog",
                "turtle",
                "whale",
                "dolphin",
                "octopus",
                "shark",
                "ant",
                "spider_web",
                "rose",
                "deciduous_tree",
                "cactus",
                "maple_leaf",
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
                "hot_pepper",
                "mushroom",
                "croissant",
                "cheese",
                "bacon",
                "pizza",
                "cookie",
                "anchor",
                "airplane",
                "crescent_moon",
                "droplet",
                "basketball",
                "8ball",
                "bell",
                "musical_note",
                "musical_keyboard",
                "telephone",
                "battery",
                "floppy_disk",
                "film_frames",
                "clapper",
                "mag_right",
                "bulb",
                "books",
                "pencil",
                "pushpin",
                "scissors",
                "key",
                "hammer",
                "bow_and_arrow",
                "dagger",
                "gear",
                "coffin",
                "crystal_ball",
                "warning",
                "no_entry",
                "radioactive",
                "question",
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

                RedBot.getEconomy().gamblingCharge(act, CHARGE);

                double rando = Math.random();
                if(CHANCE_JACKPOT > rando) {
                    msgp.reply(getJackpotWin());
                    RedBot.getEconomy().gamblingPayment(act, REWARD_JACKPOT);
                } else if(CHANCE_JACKPOT + CHANCE_HIGH > rando) {
                    msgp.reply(getHighWin());
                    RedBot.getEconomy().gamblingPayment(act, REWARD_HIGH);
                } else if(CHANCE_JACKPOT + CHANCE_HIGH + CHANCE_NORMAL > rando) {
                    msgp.reply(getNormalWin());
                    RedBot.getEconomy().gamblingPayment(act, REWARD_NORMAL);
                } else {
                    msgp.reply(getLose());
                }

            } else msgp.reply("You don't have enough money! (" + Economy.format(CHARGE) +
                    ")\nYour balance: " + Economy.MONEY_PREFIX + act.getAmount());
        }

        private static EmbedObject getLose() {
            StringBuilder sb = new StringBuilder();

            Emoji[] emojis = new Emoji[ROWS*COLS];

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    emojis[r*COLS + c] = getRandomNotIn(emojis, NORMAL_EMOJIS, HIGH_EMOJIS, JACKPOT_EMOJIS);
                }
            }

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    sb.append(emojis[r*COLS + c].getUnicode()).append(" ");
                }
                sb.append("\n");
            }

            sb.append("*Sorry! Better luck next time!*");

            return Utilities4D4J.makeEmbed("RedBot Slots Machine", sb.toString(), true);
        }

        private static EmbedObject getNormalWin() {
            StringBuilder sb = new StringBuilder();

            Emoji[] emojis = new Emoji[ROWS*COLS];

            for(int r = 0; r < ROWS; r++) {
                if(r == ROWS/2) {
                    Emoji winner = getRandomNotIn(emojis, NORMAL_EMOJIS);
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*COLS + c] = winner;
                    }
                } else {
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*COLS + c] = getRandomNotIn(emojis, NORMAL_EMOJIS, HIGH_EMOJIS, JACKPOT_EMOJIS);
                    }
                }
            }

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    sb.append(emojis[r*COLS + c].getUnicode()).append(" ");
                }
                sb.append("\n");
            }

            sb.append("**You won " + Economy.format(REWARD_NORMAL) + "!**");

            return Utilities4D4J.makeEmbed("RedBot Slots Machine", sb.toString(),true);
        }

        private static EmbedObject getHighWin() {
            StringBuilder sb = new StringBuilder();

            Emoji[] emojis = new Emoji[ROWS*COLS];

            for(int r = 0; r < ROWS; r++) {
                if(r == ROWS/2) {
                    Emoji winner = getRandomNotIn(emojis, HIGH_EMOJIS);
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*COLS + c] = winner;
                    }
                } else {
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*COLS + c] = getRandomNotIn(emojis, NORMAL_EMOJIS, HIGH_EMOJIS, JACKPOT_EMOJIS);
                    }
                }
            }

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    sb.append(emojis[r*COLS + c].getUnicode()).append(" ");
                }
                sb.append("\n");
            }

            sb.append("**You won ").append(Economy.format(REWARD_HIGH)).append("!**");

            return Utilities4D4J.makeEmbed("RedBot Slots Machine", sb.toString(), false);
        }

        private static EmbedObject getJackpotWin() {
            StringBuilder sb = new StringBuilder();

            Emoji[] emojis = new Emoji[ROWS*COLS];

            for(int r = 0; r < ROWS; r++) {
                if(r == ROWS/2) {
                    Emoji winner = getRandomNotIn(emojis, JACKPOT_EMOJIS);
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*COLS + c] = winner;
                    }
                } else {
                    for(int c = 0; c < COLS; c++) {
                        emojis[r*COLS + c] = getRandomNotIn(emojis, NORMAL_EMOJIS, HIGH_EMOJIS, JACKPOT_EMOJIS);
                    }
                }
            }

            for(int r = 0; r < ROWS; r++) {
                for(int c = 0; c < COLS; c++) {
                    sb.append(emojis[r*COLS + c].getUnicode()).append(" ");
                }
                sb.append("\n");
            }

            sb.append("**You won the jackpot: ").append(Economy.format(REWARD_JACKPOT)).append("!**");

            return Utilities4D4J.makeEmbed("RedBot Slots Machine", sb.toString(), true);
        }

        private static <T> T getRandomNotIn(T[] blacklist, T[]... options) {
            T possibility = getRandom(options);
            boolean contains = true;
            while(contains) {
                possibility = getRandom(options);
                contains = false;

                for(T one: blacklist)
                    if(possibility.equals(one)) {
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