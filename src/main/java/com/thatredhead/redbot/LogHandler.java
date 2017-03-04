package com.thatredhead.redbot;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;


public class LogHandler extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {

        IThrowableProxy proxy = iLoggingEvent.getThrowableProxy();
        if(proxy != null && proxy instanceof ThrowableProxy)
            RedBot.reportError(((ThrowableProxy) proxy).getThrowable());

        return FilterReply.NEUTRAL;
    }
}
