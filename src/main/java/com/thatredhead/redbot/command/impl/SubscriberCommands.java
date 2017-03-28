package com.thatredhead.redbot.command.impl;

import com.google.gson.reflect.TypeToken;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.thatredhead.redbot.RedBot;
import com.thatredhead.redbot.command.Command;
import com.thatredhead.redbot.command.CommandArgumentException;
import com.thatredhead.redbot.command.CommandException;
import com.thatredhead.redbot.command.CommandGroup;
import com.thatredhead.redbot.helpers4d4j.DiscordUtils;
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.permission.PermissionContext;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubscriberCommands extends CommandGroup {

    List<Subscription> subscriptions;

    public SubscriberCommands() {
        super("Subscriber Commands", "Commands for subscribing to RSS/Atom feeds", "subscriber", null);

        commands = Arrays.asList(new SubscribeCommand(),
                new SubscriptionsCommand(),
                new UnsubscribeCommand());

        subscriptions = RedBot.getDataHandler().get("subscriptions", new TypeToken<List<Subscription>>() {
        }.getType(), new ArrayList<>());

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                for (Subscription sub : subscriptions) {
                    List<SyndEntry> entries = sub.getNewEntries();
                    if (entries != null && !entries.isEmpty()) {
                        RedBot.LOGGER.debug("New entries for " + sub.getFeed().getTitle());
                        EmbedBuilder embed = new EmbedBuilder()
                                .withAuthorName(sub.feed.getTitle())
                                .withTimestamp(sub.lastUpdate);

                        if (sub.feed.getImage() != null)
                            embed.withThumbnail(sub.feed.getImage().getUrl());

                        if (entries.size() > 1)
                            for (SyndEntry entry : entries)
                                embed.appendField(
                                        entry.getTitle(),
                                        entry.getLink() != null && !entry.getLink().isEmpty() ?
                                                link(removeHtml(entry.getDescription().getValue()), entry.getLink()) :
                                                removeHtml(entry.getDescription().getValue()), false);
                        else {
                            String html = entries.get(0).getDescription().getValue();
                            embed.withTitle(entries.get(0).getTitle())
                                    .withDesc(removeHtml(html));
                            List<String> imgs = getImages(html);
                            if (!imgs.isEmpty())
                                embed.withImage(imgs.get(0));
                        }

                        DiscordUtils.sendEmbed(embed.build(), sub.getChannel());
                    }
                }
            } catch (Exception e) {
                RedBot.reportError(e);
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public class SubscribeCommand extends Command {

        public SubscribeCommand() {
            super("subscribe", "Subscribes this channel to RSS/Atom feed updates",
                    "subscribe <feed url>", PermissionContext.MOD);
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

            if (subscriptions.contains(sub))
                throw new CommandException("This channel is already subscribed to that URL!");

            subscriptions.add(sub);

            save();

            msgp.reply("Added subscription to " + sub.feed.getTitle());
        }
    }

    public class SubscriptionsCommand extends Command {

        public SubscriptionsCommand() {
            super("subscriptions", "Lists the subscriptions for this channel", PermissionContext.EVERYONE);
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
            super("unsubscribe", "Unsubscribes this channel to a subscription",
                    "unsubscribe <id (see subscriptions command)>", PermissionContext.MOD);
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
            for (; idx < subscriptions.size(); idx++) {
                if (id.equals(subscriptions.get(0).channelId))
                    count++;
                if (count == target)
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
            if (channel == null)
                channel = RedBot.getClient().getChannelByID(channelId);

            return channel;
        }

        public SyndFeed getFeed() {
            if (feed == null)
                checkFeed();

            return feed;
        }

        public List<SyndEntry> getNewEntries() {

            if (!hasUpdate()) return new ArrayList<>();

            List<SyndEntry> newEntries = new ArrayList<>();

            if (lastEntries != null) {
                for (SyndEntry entry : feed.getEntries())
                    if (!feedHas(lastEntries, entry))
                        newEntries.add(entry);
            }

            lastEntries = feed.getEntries();
            lastUpdate = feed.getPublishedDate() == null ? 0 : feed.getPublishedDate().getTime();

            return newEntries;
        }

        public boolean hasUpdate() {
            checkFeed();
            if (feed.getPublishedDate() == null)
                return true;
            return feed.getPublishedDate().after(new Date(lastUpdate));
        }

        private void checkFeed() {
            try {
                feed = new SyndFeedInput().build(new XmlReader(new URL(subscriptionUrl)));
            } catch (MalformedURLException e) {
                throw new RuntimeException();
            } catch (IOException | FeedException e) {
                RedBot.reportError(e);
            }
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Subscription && this.equals((Subscription) o);
        }

        boolean equals(Subscription sub) {
            return channelId.equals(sub.channelId) && subscriptionUrl.equals(sub.subscriptionUrl);
        }

        private boolean feedHas(List<SyndEntry> old, SyndEntry entry) {
            for (SyndEntry oldEntry : old) if (entriesSame(oldEntry, entry)) return true;
            return false;
        }

        private boolean entriesSame(SyndEntry entry1, SyndEntry entry2) {
            String link1 = entry1.getLink();
            String link2 = entry2.getLink();
            if (link1 != null && link2 != null && link1.equals(link2))
                return true;

            String title1 = entry1.getTitle();
            String title2 = entry2.getTitle();
            if (title1 != null && title2 != null && title1.equals(title2))
                return true;

            return false;
        }
    }

    private void save() {
        RedBot.getDataHandler().save(subscriptions, "subscriptions");
    }

    private static String link(String display, String link) {
        return "[" + display + "](" + link + ')';
    }

    private static final Pattern IMG_PATT = Pattern.compile("<[\\s]*img\\b.*src[\\s]*=[\\s]*\"([^\"]+)\"[^/]*/>");

    private static List<String> getImages(String html) {
        if (html == null) return Collections.singletonList("");

        Matcher m = IMG_PATT.matcher(html);

        List<String> images = new ArrayList<>();
        while (m.find()) {
            if(m.group().contains("height=\"1\"") || m.group().contains("width=\"1\"")) continue;
            images.add(m.group(1));
        }

        System.out.println(images);

        return images;
    }

    private static final Pattern HTML_PATT = Pattern.compile("<[\\s]*(\\w+)(?:(.*))?(?:/[\\s]*>|>([\\s\\S]*?)<[\\s]*/[\\s]*\\1[\\s]*>)");
    private static final Pattern LINK_PATT = Pattern.compile("[\\s]*href[\\s]*=[\\s]*\"([^\"]+)\"");
    private static final Pattern SRC_PATT = Pattern.compile("src[\\s]*=[\\s]*\"([^\"]+)\"");

    private static String removeHtml(String html) {
        if (html == null) return "";

        System.out.println(html);

        StringBuilder sb = new StringBuilder();
        Matcher m = HTML_PATT.matcher(html);

        int idx = 0;
        while (m.find(idx)) {
            // Append everything from the last parsed area to the new start
            sb.append(html.substring(idx, m.start()).trim());

            switch (m.group(1).toLowerCase()) {
                case "br":
                    sb.append('\n');
                    break;
                case "p":
                    sb.append(removeHtml(m.group(3))).append("\n\n");
                    break;
                case "b":
                    sb.append("**").append(removeHtml(m.group(3))).append("**");
                    break;
                case "u":
                    sb.append("__").append(removeHtml(m.group(3))).append("__");
                    break;
                case "i":
                    sb.append("*").append(removeHtml(m.group(3))).append("*");
                    break;
                case "a":
                    Matcher link = LINK_PATT.matcher(m.group(2));
                    if (link.find()) {
                        sb.append(link(removeHtml(m.group(3)), link.group(1)));
                    } else sb.append(removeHtml(m.group(3)));
            }
            idx = m.end();
        }

        return sb.append(html.substring(idx).trim()).toString();
    }

    public static void main(String[] args) {
        String html = "Rep. Adam Schiff, the top Democrat on the House Russia investigation, called on House Intelligence Chairman Devin Nunes to recuse himself from the investigation in a stunning split between the two top investigators.<div class=\"feedflare\">\n" +
                "<a href=\"http://rss.cnn.com/~ff/rss/cnn_topstories?a=1sBPCHEdsNU:XVgcq72MMLg:yIl2AUoC8zA\"><img src=\"http://feeds.feedburner.com/~ff/rss/cnn_topstories?d=yIl2AUoC8zA\" border=\"0\"></img></a> <a href=\"http://rss.cnn.com/~ff/rss/cnn_topstories?a=1sBPCHEdsNU:XVgcq72MMLg:7Q72WNTAKBA\"><img src=\"http://feeds.feedburner.com/~ff/rss/cnn_topstories?d=7Q72WNTAKBA\" border=\"0\"></img></a> <a href=\"http://rss.cnn.com/~ff/rss/cnn_topstories?a=1sBPCHEdsNU:XVgcq72MMLg:V_sGLiPBpWU\"><img src=\"http://feeds.feedburner.com/~ff/rss/cnn_topstories?i=1sBPCHEdsNU:XVgcq72MMLg:V_sGLiPBpWU\" border=\"0\"></img></a> <a href=\"http://rss.cnn.com/~ff/rss/cnn_topstories?a=1sBPCHEdsNU:XVgcq72MMLg:qj6IDK7rITs\"><img src=\"http://feeds.feedburner.com/~ff/rss/cnn_topstories?d=qj6IDK7rITs\" border=\"0\"></img></a> <a href=\"http://rss.cnn.com/~ff/rss/cnn_topstories?a=1sBPCHEdsNU:XVgcq72MMLg:gIN9vFwOqvQ\"><img src=\"http://feeds.feedburner.com/~ff/rss/cnn_topstories?i=1sBPCHEdsNU:XVgcq72MMLg:gIN9vFwOqvQ\" border=\"0\"></img></a>\n" +
                "</div><img src=\"http://feeds.feedburner.com/~r/rss/cnn_topstories/~4/1sBPCHEdsNU\" height=\"1\" width=\"1\" alt=\"\"/>";

        System.out.println(removeHtml(html));
    }
}