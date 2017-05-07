package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.EmojiManager;
import org.apache.commons.lang3.tuple.Pair;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecorderCommands extends CommandGroup {

    // (ChannelID, UserID) -> MessageID
    private Map<Pair<Long, Long>, Long> startMessages;

    public RecorderCommands() {
        super("Recorder Commands", "Provides tools for recording the conversation in a channel and outputting to a handy text file!",
                "recorder", null);
        commands = Arrays.asList(new RecordCommand(), new EndRecordCommand(), new ArchiveCommand());

        startMessages = RedBot.getDataHandler().get("recordings", new TypeToken<Map<Pair<Long, Long>, Long>>(){}.getType(), new HashMap<>());
    }

    public class ArchiveCommand extends Command {

        public ArchiveCommand() {
            super("archive", "Records a section of messages", "archive <first message ID> <last message ID> [pattern]", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {
            try {
                long start = Long.parseLong(msgp.getArg(1));
                long end = Long.parseLong(msgp.getArg(2));

                String pattern = msgp.getContentAfter(3).trim();
                if(pattern.length() == 0) {
                    pattern = "[%TIME%] %USER%: %CONTENT%";
                }

                File f = makeFile(pattern, start, end, msgp.getChannel(), false);
                Utilities4D4J.sendFile(f, msgp.getChannel()).get();

                f.delete();
            } catch (NumberFormatException e) {
                msgp.reply("Your IDs are not valid numbers!");
            }
        }
    }

    public class RecordCommand extends Command {

        public RecordCommand() {
            super("record", "Starts a recording of the current channel.", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {

            Pair<Long, Long> key = Pair.of(msgp.getChannel().getLongID(), msgp.getAuthor().getLongID());
            if(startMessages.containsKey(key)) {
                msgp.reply("You already have a recording going on! Use `endrecord` to stop the current recording.");
            } else {
                startMessages.put(key, msgp.getMsg().getLongID());
                Utilities4D4J.addReactions(msgp.getMsg(), EmojiManager.getByUnicode("\uD83D\uDC4C"));
            }
        }
    }

    public class EndRecordCommand extends Command {

        public EndRecordCommand() {
            super("endrecord", "Ends a recording of the current channel.", "endrecord [pattern (%USER%, %CONTENT%, %TIME%, %DATE% usable)]", PermissionContext.EVERYONE);
        }

        @Override
        public void invoke(MessageParser msgp) throws CommandException {

            Pair<Long, Long> key = Pair.of(msgp.getChannel().getLongID(), msgp.getAuthor().getLongID());
            if(startMessages.containsKey(key)) {
                long start = startMessages.remove(key);
                long end = msgp.getMsg().getLongID();
                String pattern = msgp.getContentAfter(1).trim();
                if(pattern.length() == 0) {
                    pattern = "[%TIME%] %USER%: %CONTENT%";
                }

                File f = makeFile(pattern, start, end, msgp.getChannel(), false);
                Utilities4D4J.sendFile(f, msgp.getChannel()).get();

                f.delete();

            } else {
                msgp.reply("You don't have a recording going on! Use `record` to start a recording.");
            }
        }
    }

    private static File makeFile(String pattern, long start, long end, IChannel c, boolean include) {
        File f = new File("data/records/record.txt");
        int count = 0;
        while(f.exists()) {
            f = new File("data/records/record" + ++count + ".txt");
        }

        try(BufferedWriter w = new BufferedWriter(new FileWriter(f))) {

            for(IMessage msg : c.getHistory()
                    .startAt(start, include)
                    .endAt(end, include)) {
                w.write(format(msg, pattern));
                w.newLine();
            }
        } catch(IOException e) {
            RedBot.reportError(e);
        }

        return f;
    }

    private static Pattern p = Pattern.compile("(%USER%|%CONTENT%|%TIME%|%DATE%)");

    private static String format(IMessage msg, String pattern) {
        Matcher m = p.matcher(pattern);
        StringBuffer result = new StringBuffer();
        while(m.find()) {
            switch(m.group(1)) {
                case "%USER%":
                    m.appendReplacement(result, msg.getAuthor().getDisplayName(msg.getGuild())); break;
                case "%CONTENT%":
                    m.appendReplacement(result, msg.getContent()); break;
                case "%TIME%":
                    m.appendReplacement(result, time(msg.getTimestamp())); break;
                case "%DATE%":
                    m.appendReplacement(result, date(msg.getTimestamp())); break;
            }
        }
        return m.appendTail(result).toString();
    }

    private static String time(LocalDateTime t) {
        return pad(t.get(ChronoField.HOUR_OF_AMPM), 2) + ":" +
                pad(t.get(ChronoField.MINUTE_OF_HOUR), 2) +
                (t.get(ChronoField.AMPM_OF_DAY) == 0 ? " AM" : " PM");
    }

    private static String date(LocalDateTime t) {
        return pad(t.get(ChronoField.YEAR), 4) + "-" +
                pad(t.get(ChronoField.MONTH_OF_YEAR), 2) + "-" +
                pad(t.get(ChronoField.DAY_OF_MONTH), 2);
    }

    private static String pad(int num, int length) {
        return String.format("%0" + length + "d", num);
    }
}
