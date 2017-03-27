package com.thatredhead.redbot.helpers4d4j;

import com.thatredhead.redbot.RedBot;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Collection of useful D4J wrapper methods
 */
public class DiscordUtils {

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

    public static RequestBuffer.RequestFuture<IMessage> edit(IMessage msg, String newContent) {
        if(newContent == null || msg == null)
            throw new NullPointerException();
        readyCheck();
        return RequestBuffer.request(() -> {
            return msg.edit(newContent);
        });
    }

    public static RequestBuffer.RequestFuture<IMessage> edit(String msgID, String newContent) {
        return edit(RedBot.getClient().getMessageByID(msgID), newContent);
    }

    private static void readyCheck() {
        if(!RedBot.isReady())
            try {
                RedBot.getClient().getDispatcher().waitFor(ReadyEvent.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}