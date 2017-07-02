package com.thatredhead.redbot.command.impl;

import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import org.apache.commons.io.IOUtils;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReadCommand extends Command {

    public ReadCommand() {
        super("read", "Reads a file into the channel!", PermissionContext.EVERYONE);
    }

    public void invoke(MessageParser msgp) {
        List<IMessage.Attachment> att = msgp.getMsg().getAttachments();

        if (att == null || att.isEmpty()) {
            msgp.reply("Attach a file for it to be read.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (IMessage.Attachment a : att) {
                try {
                    URLConnection c = new URL(a.getUrl()).openConnection();
                    c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
                    sb.append(IOUtils.toString(c.getInputStream(), "UTF-8")).append("\n");
                } catch (IOException e) {
                    RedBot.reportError(e);
                    msgp.reply("Something went wrong reading this attachment! Check RedBot's support guild for further information.");
                    return;
                }
            }
            if (sb.length() < 1) {
                msgp.reply("There's no content in the attached file(s)!");
            } else if (sb.length() > 20000) {
                msgp.reply("The content of these files will require more than " + (int) Math.ceil(sb.length()/2000.0) + " messages to display. Respond with \"Yes\" in the next 10 seconds to confirm.");

                IListener l = e -> {
                    if (e instanceof MessageReceivedEvent) {
                        IMessage m = ((MessageReceivedEvent) e).getMessage();
                        if (m.getAuthor().equals(msgp.getAuthor()) && m.getContent().equalsIgnoreCase("yes")) {
                            print(msgp.getChannel(), sb.toString().trim());
                            RedBot.getClient().getDispatcher().unregisterListener(this);
                        }
                    }
                };
                RedBot.getClient().getDispatcher().registerListener(l);
                Executors.newSingleThreadScheduledExecutor().schedule(() -> RedBot.getClient().getDispatcher().unregisterListener(l), 10, TimeUnit.SECONDS);
            } else {
                print(msgp.getChannel(), sb.toString().trim());
            }
        }
    }

    private static void print(IChannel c, String s) {
        int i = 0;
        while (i < s.length()) {
            String chunk = s.substring(i, i = findBreak(s, i));
            Utilities4D4J.sendMessage(chunk, c).get();
        }
    }

    private static final char[] CHECKS = {'\n', '.', ' '};
    private static int findBreak(String s, int idx) {
        if (idx + 2000 > s.length()) {
            return s.length();
        } else {
            String sub = s.substring(idx, idx + 2000);
            int i;
            for (char c : CHECKS) {
                i = sub.lastIndexOf(c);
                if (i >= 0) return i + idx + 1;
            }
            return idx + 2000;
        }
    }
}
