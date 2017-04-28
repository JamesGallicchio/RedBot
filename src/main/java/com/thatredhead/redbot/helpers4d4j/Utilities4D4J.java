package com.thatredhead.redbot.helpers4d4j;

import com.thatredhead.redbot.RedBot;
import com.vdurmont.emoji.Emoji;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionRemoveEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Collection of useful D4J wrapper methods
 */
public class Utilities4D4J {

    private static Map<Long, ReactionListener> reactions = new HashMap<>();

    /**
     * Sends a message to a channel through the request buffer
     * @param msg message to send
     * @param channel channel to send msg to
     * @return request future for the message sent
     */
    public static RequestBuffer.RequestFuture<IMessage> sendMessage(String msg, IChannel channel) {
        if(channel == null || msg == null)
            throw new NullPointerException();
        readyCheck();
        return RequestBuffer.request(() -> {
            return channel.sendMessage(msg);
        });
    }

    /**
     * Sends a message to a channel through the request buffer by sending a ZWSP,
     * and then editing it to include the message
     * @param msg message to send
     * @param channel channel to send msg to
     * @return request future for the message sent
     */
    public static RequestBuffer.RequestFuture<IMessage> sendMessageAsEdit(String msg, IChannel channel) {
        if(channel == null || msg == null)
            throw new NullPointerException();
        readyCheck();
        return RequestBuffer.request(() -> {
            return RequestBuffer.request(() -> {
                return channel.sendMessage("\u200B");
            }).get().edit(msg);
        });
    }

    /**
     * Sends a private message to a user through the request buffer
     * @param msg message to send
     * @param user user to PM msg to
     * @return request future for the message sent
     */
    public static RequestBuffer.RequestFuture<IMessage> sendPrivateMessage(String msg, IUser user) {
        if(user == null || msg == null)
            throw new NullPointerException();
        readyCheck();
        return RequestBuffer.request(() -> {
            return user.getOrCreatePMChannel().sendMessage(msg);
        });
    }

    public static RequestBuffer.RequestFuture<IMessage> sendPrivateMessage(EmbedObject embed, IUser user) {
        if(user == null || embed == null)
            throw new NullPointerException();
        readyCheck();
        return RequestBuffer.request(() -> {
            return user.getOrCreatePMChannel().sendMessage(embed);
        });
    }

    /**
     * Sends an embed to a channel
     * @param embed the embed to send
     * @param channel the channel to send the embed to
     * @return a future for the message
     */
    public static RequestBuffer.RequestFuture<IMessage> sendEmbed(EmbedObject embed, IChannel channel) {
        if(channel == null || embed == null)
            throw new NullPointerException();
        readyCheck();
        return RequestBuffer.request(() -> {
            return channel.sendMessage(embed);
        });
    }

    public static RequestBuffer.RequestFuture<IMessage> sendEmbed(IChannel channel, String title, String description, boolean inline, String... fields) {
        return sendEmbed(Utilities4D4J.makeEmbed(title, description, inline, fields), channel);
    }

    /**
     * Sends a temporary message to a channel that lasts 30 seconds
     * @param msg message to send
     * @param channel channel to send msg to
     */
    public static void sendTemporaryMessage(String msg, IChannel channel) {
        sendTemporaryMessage(msg, channel, 30000);
    }

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Sends a temporary message to a channel
     * @param msg message to send
     * @param channel channel to send msg to
     * @param milliDelay length of time to wait before deleting the message
     */
    public static void sendTemporaryMessage(String msg, IChannel channel, int milliDelay) {
        if(channel == null || msg == null)
            throw new NullPointerException();
        readyCheck();
        RequestBuffer.RequestFuture<IMessage> message = sendMessage(msg + "\n\n*This message may be deleted.*", channel);
        scheduler.schedule(() -> RequestBuffer.request(() -> {
                    try {
                        message.get().delete();
                    } catch (DiscordException | MissingPermissionsException e) {
                        RedBot.reportError(e);
                    }
                }), milliDelay, TimeUnit.MILLISECONDS);
    }

    public static IMessage addReactions(IMessage msg, Emoji... emojis) {
        for(Emoji e: emojis)
            RequestBuffer.request(() -> msg.addReaction(e));

        return msg;
    }

    public static IMessage addReactionsOrdered(IMessage msg, Emoji... emojis) {
        for(Emoji e: emojis)
            RequestBuffer.request(() -> msg.addReaction(e)).get();

        return msg;
    }

    public interface ReactionListener {
        default void onReactionAdd(IMessage msg, IUser user, Emoji emoji) {
            onReactionToggle(msg, user, emoji);
        }
        default void onReactionRemove(IMessage msg, IUser user, Emoji emoji) {
            onReactionToggle(msg, user, emoji);
        }
        default void onReactionToggle(IMessage msg, IUser user, Emoji emoji) {}
    }

    public static IMessage sendReactionUI(String msg, IChannel channel, ReactionListener listener, Emoji... emojis) {
        return addReactionUI(
                addReactionsOrdered(sendMessage(msg, channel).get(), emojis),
                listener
        );
    }

    public static IMessage sendReactionUI(EmbedObject msg, IChannel channel, ReactionListener listener, Emoji... emojis) {
        return addReactionUI(
                addReactionsOrdered(sendEmbed(msg, channel).get(), emojis),
                listener
        );
    }

    public static IMessage addReactionUI(IMessage msg, ReactionListener l) {
        reactions.put(msg.getLongID(), l);

        return msg;
    }

    public static boolean removeReactionUI(long messageID) {
        return reactions.remove(messageID) != null;
    }

    @EventSubscriber
    public static void onReactionAdd(ReactionAddEvent e) {
        ReactionListener l = reactions.get(e.getMessage().getLongID());
        l.onReactionAdd(e.getMessage(), e.getUser(), e.getReaction().getUnicodeEmoji());
    }

    @EventSubscriber
    public static void onReactionRemove(ReactionRemoveEvent e) {
        ReactionListener l = reactions.get(e.getMessage().getLongID());
        l.onReactionRemove(e.getMessage(), e.getUser(), e.getReaction().getUnicodeEmoji());
        l.onReactionToggle(e.getMessage(), e.getUser(), e.getReaction().getUnicodeEmoji());
    }

    public static RequestBuffer.RequestFuture<IMessage> edit(IMessage msg, String newContent) {
        if(newContent == null || msg == null)
            throw new NullPointerException();
        readyCheck();
        return RequestBuffer.request(() -> {
            return msg.edit(newContent);
        });
    }

    public static RequestBuffer.RequestFuture<IMessage> edit(IMessage msg, EmbedObject newEmbed) {
        if(newEmbed == null || msg == null)
            throw new NullPointerException();
        readyCheck();
        return RequestBuffer.request(() -> {
            return msg.edit(newEmbed);
        });
    }

    public static RequestBuffer.RequestFuture<IMessage> edit(IMessage msg, String title, String description, boolean inline, String... fields) {
        return edit(msg, Utilities4D4J.makeEmbed(title, description, inline, fields));
    }

    public static EmbedObject makeEmbed(String title, String description, boolean inline, String... fields) {
        EmbedBuilder embed = new EmbedBuilder()
                .withTitle(title)
                .withDesc(description)
                .withAuthorName("RedBot")
                .withAuthorIcon(RedBot.getClient().getOurUser().getAvatarURL())
                .withTimestamp(LocalDateTime.now())
                .withColor(Color.RED);

        for(int i = 1; i < fields.length; i += 2) {
            embed.appendField(fields[i-1], fields[i], inline);
        }

        return embed.build();
    }

    private static void readyCheck() {
        if(!RedBot.isReady())
            try {
                RedBot.getClient().getDispatcher().waitFor(ReadyEvent.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    public static class SerializableMessage {

        private long channelID;
        private long messageID;

        public SerializableMessage() {}

        public SerializableMessage(IMessage msg) {
            channelID = msg.getChannel().getLongID();
            messageID = msg.getLongID();
        }

        public IMessage get() {
            return RedBot.getClient().getChannelByID(channelID).getMessageByID(messageID);
        }

        public long getID() {
            return messageID;
        }
    }

    public static IMessage getMessageByID(long id) {
        IMessage msg = RedBot.getClient().getMessageByID(id);

        if(msg == null) {
            for (IChannel c : RedBot.getClient().getChannels()) {
                msg = c.getMessageByID(id);
                if (msg != null) return msg;
            }
        }

        return null;
    }
}