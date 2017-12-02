package com.thatredhead.redbot.command.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import com.thatredhead.redbot.helpers4d4j.MessageParser;
import com.thatredhead.redbot.helpers4d4j.Utilities4D4J;
import com.thatredhead.redbot.permission.PermissionContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class SubscriberCommands extends CommandGroup {

    public static final int UPDATE_PERIOD = 2*60*1000;

    public List<SubscriptionFeed> subscriptions;
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(4);

    public SubscriberCommands() {
        super("Subscriber Commands", "Commands for subscribing to RSS/Atom feeds", "subscriber", null);

        commands = Arrays.asList(new SubscribeCommand(),
                new SubscriptionsCommand(),
                new UnsubscribeCommand());

        subscriptions = RedBot.getDataHandler().get("subscriptions", new TypeToken<List<SubscriptionFeed>>(){}.getType(),
                new ArrayList<SubscriptionFeed>());

        exec.scheduleAtFixedRate(() -> {
            int pause = UPDATE_PERIOD/subscriptions.size();
            int wait = 0;
            for (SubscriptionFeed s : subscriptions) {
                wait += pause;
                exec.schedule(s::tick, wait, TimeUnit.MILLISECONDS);
            }
        }, 0, UPDATE_PERIOD, TimeUnit.MILLISECONDS);
    }

    private SubscriptionFeed getOrAdd(String url) throws MalformedURLException {
        URL url2 = new URL(url);

        Optional<SubscriptionFeed> sub = subscriptions.stream().filter(f -> f.url.sameFile(url2)).findFirst();
        if (sub.isPresent()) return sub.get();

        SubscriptionFeed feed = new SubscriptionFeed(url);
        subscriptions.add(feed);

        return feed;
    }

    private List<SubscriptionFeed> channelSubs(long id) {
        return subscriptions.stream().filter(f -> f.channels.contains(id)).collect(Collectors.toList());
    }

    private void save() {
        RedBot.getDataHandler().save(subscriptions, "subscriptions");
    }

    public class SubscribeCommand extends Command {

        public SubscribeCommand() {
            super("subscribe", "Subscribes this channel to RSS/Atom feed updates",
                    "subscribe <feed url>", PermissionContext.MOD);
        }

        @Override
        public void invoke(MessageParser msgp) {

            SubscriptionFeed sub;
            try {
                sub = getOrAdd(msgp.getArg(1));
            } catch (MalformedURLException e) {
                throw new CommandException("Invalid RSS Feed: " + msgp.getArg(1));
            }

            if (sub.channels.contains(msgp.getChannel().getLongID()))
                throw new CommandException("This channel is already subscribed to that URL!");

            sub.channels.add(msgp.getChannel().getLongID());

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
            long id = msgp.getChannel().getLongID();

            List<SubscriptionFeed> subs = channelSubs(id);

            if (subs.isEmpty()) msgp.reply("There are no subscriptions in this channel.");
            else {
                StringBuilder sb = new StringBuilder("```md\n");
                int count = 0;

                for (SubscriptionFeed sub : subs)
                    sb.append("[").append(count++).append("]: ").append(sub.feed == null ? "<UNKNOWN>" : sub.feed.getTitle()).append("\n");

                msgp.reply(sb.append("```").toString());
            }
        }
    }

    public class UnsubscribeCommand extends Command {

        public UnsubscribeCommand() {
            super("unsubscribe", "Unsubscribes this channel to a subscription",
                    "unsubscribe <id (see subscriptions command)>", PermissionContext.MOD);
        }

        @Override
        public void invoke(MessageParser msgp) {
            long id = msgp.getChannel().getLongID();

            try {
                int target = Integer.parseInt(msgp.getArg(1));

                List<SubscriptionFeed> subs = channelSubs(id);
                if (target < 0 || target >= subs.size()) throw new NumberFormatException();

                SubscriptionFeed sub = subs.get(target);
                sub.channels.remove(id);
                save();

                msgp.reply("Removed subscription to " + (sub.feed == null ? "<UNKNOWN>" : sub.feed.getTitle()));

            } catch (NumberFormatException e) {
                throw new CommandArgumentException(0, msgp.getArg(0), usage);
            }
        }
    }

    public static class SubscriptionFeed {
        public static final int START_WAIT =   2*60;

        public static final JsonDeserializer<SubscriptionFeed> DESERIALIZER = (jsonElement, type, jsonDeserializationContext) -> {

            JsonObject obj = jsonElement.getAsJsonObject();
            String sub = obj.getAsJsonPrimitive("subUrl").getAsString();
            JsonArray channels = obj.getAsJsonArray("channels");

            try {
                SubscriptionFeed feed = new SubscriptionFeed(sub);
                for (JsonElement e : channels) {
                    feed.channels.add(e.getAsLong());
                }
                return feed;
            } catch (MalformedURLException e) { return null; }
        };

        private static SyndFeedInput io = new SyndFeedInput();

        final String subUrl;
        final Set<Long> channels = new HashSet<>();

        private transient URL url;
        private transient SyndFeed feed;

        public SubscriptionFeed(String sub) throws MalformedURLException {
            subUrl = sub;

            url = new URL(subUrl);

            updateFeed();
        }

        private void updateFeed() {
            try {
                feed = io.build(new XmlReader(url));
            } catch (IOException | FeedException e) {
                RedBot.reportError(e);
            }
        }

        private List<SyndEntry> newEntries() {
            List<SyndEntry> old = feed.getEntries();
            updateFeed();
            return feed.getEntries().stream().filter(e -> {
                for (SyndEntry eOld : old) {
                    if (entryEqual(e, eOld)) return false;
                }
                return true;
            }).collect(Collectors.toList());
        }

        private void tick() {
            List<SyndEntry> newEntries = newEntries();

            if (!newEntries.isEmpty()) {
                EmbedObject toSend = new FeedEmbed(feed, newEntries).toEmbed();

                channels.forEach(id -> Utilities4D4J.sendEmbed(toSend, RedBot.getClient().getChannelByID(id)));
            }
        }

        private static int randomWait() {
            return START_WAIT + (int) ((Math.random() - 0.5) * 60);
        }

        private static boolean entryEqual(SyndEntry e1, SyndEntry e2) {
            String link1 = e1.getLink();
            String link2 = e2.getLink();
            if (link1 != null && link2 != null && link1.equals(link2))
                return true;

            String title1 = e1.getTitle();
            String title2 = e2.getTitle();
            return title1 != null && title2 != null && title1.equals(title2);
        }
    }

    static class FeedEmbed {
        private EmbedBuilder embed;

        private String img = "";
        private String alt = "";

        public FeedEmbed(SyndFeed feed, List<SyndEntry> entries) {
            embed = new EmbedBuilder()
                    .withAuthorName(feed.getTitle());

            if (feed.getImage() != null)
                embed.withThumbnail(feed.getImage().getUrl());

            if (entries.size() > 1) {
                for (SyndEntry e : entries) {
                    embed.appendField(e.getTitle(),
                            limitLink(
                                    build(e.getDescription().getValue()),
                                    e.getLink(), EmbedBuilder.FIELD_CONTENT_LIMIT
                            ),true);
                }
                embed.withImage(img)
                        .withFooterText(alt);
            } else {
                embed.withTitle(entries.get(0).getTitle())
                        .withDescription(
                                limitLink(
                                        build(entries.get(0).getDescription().getValue()),
                                        entries.get(0).getLink(), EmbedBuilder.DESCRIPTION_CONTENT_LIMIT
                                )
                        )
                        .withFooterText(alt)
                        .withImage(img)
                        .withTimestamp(entries.get(0).getPublishedDate().toInstant().toEpochMilli());
            }
        }

        private String build(String html) {
            return rec(Jsoup.parseBodyFragment(html).body());
        }

        private String rec(List<Node> nodes) {
            StringBuilder txt = new StringBuilder();
            for (Node n : nodes) txt.append(rec(n));
            return txt.toString();
        }

        private String rec(Node node) {
            if (node instanceof TextNode) {
                return ((TextNode) node).text();

            } else {
                String inner = rec(node.childNodes());

                if (node instanceof Element) {
                    switch (((Element) node).tagName()) {
                        case "img":
                            setBig(node.attributes().get("src"), node.attributes().get("title"));
                            return "";
                        case "a":
                            String link = node.attributes().get("href");
                            if (link == null || link.isEmpty() || inner.isEmpty()) return "";
                            return "[" + inner + "](" + link + ")";
                        case "b":
                            return "**" + inner + "**";
                        case "u":
                            return "__" + inner + "__";
                        case "i":
                            return "*" + inner + "*";
                        case "h1":
                        case "h2":
                        case "h3":
                        case "h4":
                        case "h5":
                            return "\n\n**" + inner + "**\n";
                        case "p":
                            return "\n\n";
                        case "br":
                            return "\n";
                    }
                }

                return inner;
            }
        }

        private void setBig(String img, String alt) {
            if (img == null || img.isEmpty()) return;

            if (this.img == null || this.img.isEmpty()) {
                this.img = img;
                this.alt = alt;
            }
        }

        public EmbedObject toEmbed() {
            return embed.build();
        }

        private static String limitLink(String text, String link, int length) {
            if (text.length() > length-8)
                text = text.substring(0, length-8);

            return text + "... [more](" + link + ")";
        }
    }
}