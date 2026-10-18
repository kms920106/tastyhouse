package com.tastyhouse.testsupport.shared.logging;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

public final class LogCapture {

    private final Logger logger;
    private final ListAppender<ILoggingEvent> appender;

    private LogCapture(Logger logger, ListAppender<ILoggingEvent> appender) {
        this.logger = logger;
        this.appender = appender;
    }

    public static LogCapture attachTo(Class<?> target) {
        Logger logger = (Logger) LoggerFactory.getLogger(target);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);
        return new LogCapture(logger, appender);
    }

    public void detach() {
        logger.detachAppender(appender);
        appender.stop();
    }

    public List<ILoggingEvent> events() {
        return List.copyOf(appender.list);
    }

    public String singleFormattedMessage() {
        List<ILoggingEvent> events = events();
        if (events.size() != 1) {
            throw new AssertionError("로그가 정확히 1건이어야 합니다. 실제: " + events.size() + "건");
        }
        return events.getFirst().getFormattedMessage();
    }
}
