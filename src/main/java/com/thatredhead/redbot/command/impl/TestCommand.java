package com.thatredhead.redbot.command.impl;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", "TESTY COMMAND", PermissionContext.BOT_OWNER);
    }

    @Override
    public void invoke(MessageParser msgp) throws CommandException {
        String subUrl = msgp.getContentAfter(1);

        try {
            URL url = new URL(subUrl);

            SyndFeed feed = new SyndFeedInput().build(new XmlReader(url));

            for (int i = 5; i < feed.getEntries().size(); i++) {
                List<SyndEntry> entries = feed.getEntries().subList(i, i+1);

                msgp.getChannel().sendMessage(new SubscriberCommands.FeedEmbed(feed, entries).toEmbed());
            }
        } catch (IOException | FeedException e) {
            e.printStackTrace();
        }
    }
}
