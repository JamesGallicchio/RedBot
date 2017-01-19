package com.thatredhead.redbot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by JGallicchio on 1/18/2017.
 */
public class DiscordUtils {

    public static RequestBuffer.RequestFuture<IMessage> sendMessage(String msg, IChannel channel) {
        return RequestBuffer.request(() -> {
            try {
                return channel.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static void sendTemporaryMessage(String msg, IChannel channel) {
        sendTemporaryMessage(msg, channel, 30000);
    }

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static void sendTemporaryMessage(String msg, IChannel channel, int milliDelay) {
        IMessage message = sendMessage(msg + "\n\n*This message may be deleted.*", channel).get();
        scheduler.schedule(() -> RequestBuffer.request(() -> {
                    try {
                        message.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }), milliDelay, TimeUnit.MILLISECONDS);
    }
}
