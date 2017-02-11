package com.thatredhead.redbot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordUtils {

    public static RequestBuffer.RequestFuture<IMessage> sendMessage(String msg, IChannel channel) {
        return RequestBuffer.request(() -> {
            return channel.sendMessage(msg);
        });
    }

    public static RequestBuffer.RequestFuture<IMessage> sendPrivateMessage(String msg, IUser user) {
        return RequestBuffer.request(() -> {
            return user.getOrCreatePMChannel().sendMessage(msg);
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
