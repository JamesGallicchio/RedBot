package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.thatredhead.redbot.DiscordUtils;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.*;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

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
        commands = Arrays.asList(new SubscribeCommand(),
                                 new SubscriptionsCommand(),
                                 new UnsubscribeCommand());

        subscriptions = RedBot.getDataHandler().get("subscriptions", new TypeToken<List<Subscription>>(){}.getType(), new ArrayList<>());

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("Polling subscriptions...");
            for(Subscription sub : subscriptions) {
                List<SyndEntry> entries = sub.getNewEntries();
                if(entries != null) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .withAuthorName(sub.feed.getTitle())
                            .withThumbnail(sub.feed.getImage().getUrl())
                            .withTimestamp(sub.lastUpdate);

                    if(entries.size() > 1)
                        for (SyndEntry entry : entries)
                            embed.appendField(entry.getTitle(), entry.getDescription().getValue(), false);
                    else
                        embed.withTitle(entries.get(0).getTitle())
                                .withDesc(entries.get(0).getDescription().getValue());

                    DiscordUtils.sendEmbed(embed.build(), sub.getChannel());
                }
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
            Subscription sub;
            try {
                sub = new Subscription(msgp.getArg(1), msgp.getChannel());
            } catch (RuntimeException e) {
                msgp.reply("Invalid URL: " + msgp.getArg(1));
                return;
            }

            if(subscriptions.contains(sub))
                throw new CommandException("This channel is already subscribed to that URL!");

            subscriptions.add(sub);

            save();

            msgp.reply("Added subscription to " + sub.feed.getTitle());
        }
    }

    public class SubscriptionsCommand extends Command {

        public SubscriptionsCommand() {
            keyword = usage = permission = "subscriptions";
            description = "Lists the subscriptions for this channel";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return PermissionContext.getEveryoneContext();
        }

        @Override
        public void invoke(MessageParser msgp) {
            StringBuilder sb = new StringBuilder("```md\n");

            String id = msgp.getChannel().getID();

            int count = 0;
            boolean has = false;
            for (Subscription sub : subscriptions)
                if (id.equals(sub.channelId)) {
                    has = true;
                    sb.append("[").append(count++).append("]: ").append(sub.getFeed().getTitle()).append("\n");
                }

            msgp.reply(has ? sb.append("```").toString() : "There are no subscriptions in this channel.");
        }
    }

    public class UnsubscribeCommand extends Command {

        public UnsubscribeCommand() {
            keyword = permission = "unsubscribe";
            description = "Unsubscribes this channel to a subscription";
            usage = "unsubscribe <id (see subscriptions command)>";
        }

        @Override
        public PermissionContext getDefaultPermissions() {
            return new PermissionContext(Permissions.ADMINISTRATOR);
        }

        @Override
        public void invoke(MessageParser msgp) {
            String id = msgp.getChannel().getID();

            int target;
            try {
                target = Integer.parseInt(msgp.getArg(1));
            } catch (NumberFormatException e) {
                throw new CommandArgumentException(0, msgp.getArg(0), usage);
            }

            int count = -1;
            int idx = 0;
            for(; idx < subscriptions.size(); idx++) {
                if (id.equals(subscriptions.get(0).channelId))
                    count++;
                if(count == target)
                    break;
            }

            Subscription sub = subscriptions.remove(idx);

            save();

            msgp.reply("Removed subscription to " + sub.feed.getTitle());
        }
    }

    private static class Subscription {

        private String channelId;
        private String subscriptionUrl;
        private long lastUpdate;

        private transient SyndFeed feed;
        private transient List<SyndEntry> lastEntries;
        private transient IChannel channel;


        public Subscription(String url, IChannel channel) {
            channelId = channel.getID();
            this.channel = channel;
            subscriptionUrl = url;
            checkFeed();
        }

        public IChannel getChannel() {
            if(channel == null)
                channel = RedBot.getClient().getChannelByID(channelId);

            return channel;
        }

        public SyndFeed getFeed() {
            if(feed == null)
                checkFeed();

            return feed;
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

    private void save() {
        RedBot.getDataHandler().save(subscriptions, "subscriptions");
    }

    private String link(String display, String link) {
        return "[" + display + "](" + link  + ')';
    }
}