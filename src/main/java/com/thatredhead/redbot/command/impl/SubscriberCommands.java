package com.thatredhead.redbot.command.impl;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.command.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.Permissions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SubscriberCommands extends CommandGroup {

    List<Subscription> subscriptions;

    public SubscriberCommands() {
        name = "Subscriber Commands";
        description = "Commands for subscribing to RSS/Atom feeds";
        permission = "subscriber";
        commands = Arrays.asList(new SubscribeCommand());

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for(Subscription sub : subscriptions) {
                
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public class SubscribeCommand extends Command {

        public SubscribeCommand() {
            keyword = usage = permission = "subscribe";
            description = "Subscribes this channel to RSS/Atom feed updates";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return new PermissionContext(Permissions.ADMINISTRATOR);
        }

        @Override
        public void invoke(MessageParser msgp) {
            SubscriberCommands.Subscription sub = new SubscriberCommands.Subscription(msgp.getArg(1), msgp.getChannel());

            if(subscriptions.contains(sub))
                throw new CommandException("This channel is already subscribed to that URL!");

            subscriptions.add(sub);
        }
    }

    private static class Subscription {

        private String channelId;
        private String subscriptionUrl;
        private long lastUpdate;

        private transient SyndFeed feed;
        private transient List<SyndEntry> lastEntries;


        public Subscription(String url, IChannel channel) {
            channelId = channel.getID();
            subscriptionUrl = url;
            checkFeed();
        }

        public List<SyndEntry> getNewEntries() {

            if(!hasUpdate()) return new ArrayList<>();

            List<SyndEntry> newEntries = new ArrayList<>();

            if(lastEntries != null) {
                for (SyndEntry entry : feed.getEntries())
                    if (!lastEntries.contains(entry))
                        newEntries.add(entry);
            }

            lastEntries = feed.getEntries();
            lastUpdate = feed.getPublishedDate().getTime();

            return newEntries;
        }

        public boolean hasUpdate() {
            checkFeed();
            return feed.getPublishedDate().after(new Date(lastUpdate));
        }

        private void checkFeed() {
            try {
                feed = new SyndFeedInput().build(new XmlReader(new URL(subscriptionUrl)));
            } catch (MalformedURLException e) {
                throw new RuntimeException();
            } catch (IOException | FeedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Subscription && this.equals((Subscription) o);
        }

        boolean equals(Subscription sub) {
            return channelId.equals(sub.channelId) && subscriptionUrl.equals(sub.subscriptionUrl);
        }
    }
}