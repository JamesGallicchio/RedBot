package com.thatredhead.redbot;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.eclipse.jetty.io.EofException;


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
}
