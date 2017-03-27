package com.thatredhead.redbot;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.eclipse.jetty.io.EofException;
import org.slf4j.LoggerFactory;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectSuccessEvent;


public class LogHandler extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {

        IThrowableProxy proxy = iLoggingEvent.getThrowableProxy();
        if(proxy != null && proxy instanceof ThrowableProxy) {
            Throwable e = ((ThrowableProxy) proxy).getThrowable();
            if(!(e instanceof EofException))
                RedBot.reportError(e);
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
}
