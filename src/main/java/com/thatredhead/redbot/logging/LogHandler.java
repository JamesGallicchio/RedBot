package com.thatredhead.redbot.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.thatredhead.redbot.RedBot;
import org.eclipse.jetty.io.EofException;
import org.slf4j.LoggerFactory;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LogHandler extends Filter<ILoggingEvent> {

    private static final long MILLI_LOG_PERSISTENCE = 1000L * 60 * 60 * 24 * 7; // 7 days
    private static final File LOG_FILE = new File("logs/latest.log");

    static {
        if (LOG_FILE.exists()) {
            LOG_FILE.delete();
        }
    }

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {

        IThrowableProxy proxy = iLoggingEvent.getThrowableProxy();
        if (proxy != null && proxy instanceof ThrowableProxy) {
            Throwable e = ((ThrowableProxy) proxy).getThrowable();
            if (!(e instanceof EofException)) {
                RedBot.reportError(e);
                return FilterReply.DENY;
            }
        }

        return FilterReply.NEUTRAL;
    }

    @EventSubscriber
    public void onDisconnect(DisconnectedEvent event) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger("org.eclipse.jetty.websocket").setLevel(Level.TRACE);
        context.getLogger(Discord4J.class).setLevel(Level.TRACE);
    }

    @EventSubscriber
    public void onReconnectSuccess(ReconnectSuccessEvent event) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger("org.eclipse.jetty.websocket").setLevel(Level.DEBUG);
        context.getLogger(Discord4J.class).setLevel(Level.DEBUG);
    }

    public static void saveLogFile() {
        if (LOG_FILE.exists()) {
            try {
                Files.copy(LOG_FILE.toPath(),
                        Paths.get("logs/" +
                                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(
                                        new Date(LOG_FILE.lastModified())) +
                                ".log"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOG_FILE.deleteOnExit();

        for (File f : new File("logs").listFiles())
            if (System.currentTimeMillis() - f.lastModified() > MILLI_LOG_PERSISTENCE)
                f.deleteOnExit();
    }
}
