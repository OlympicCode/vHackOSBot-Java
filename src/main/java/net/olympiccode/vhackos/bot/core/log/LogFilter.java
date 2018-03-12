package net.olympiccode.vhackos.bot.core.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LogFilter extends TurboFilter {
    private String loggerName = "org.reflections.Reflections";
    private String last = "";
    int dupe = 0;
    @Override
    public FilterReply decide(Marker marker, Logger logger,
                              Level level, String format, Object[] params, Throwable t) {
        if (!logger.getName().equals(loggerName) && last.equals(format + paramsAsString(params, logger))) {
            if (dupe > 1) {
                return FilterReply.DENY;
            } else {
                dupe++;
            }
        } else {
            last = format;
            dupe = 0;
        }
        if (loggerName == null) {
            return FilterReply.NEUTRAL;
        } else if (loggerName.equals(logger.getName())) {
            return FilterReply.DENY;
        } else
            return FilterReply.NEUTRAL;
    }

    private String paramsAsString(final Object[] params, final Logger logger) {
        if (params != null) {
            return Arrays.stream(params).map(Object::toString).collect(Collectors.joining("_"));
        }

        return "";
    }
}