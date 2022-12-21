package io.github.jmmedina00.adoolting.config.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class DebugOnlyFilter extends AbstractMatcherFilter<LoggingEvent> {

  @Override
  public FilterReply decide(LoggingEvent event) {
    return event.getLevel().toInt() == Level.DEBUG_INT
      ? FilterReply.NEUTRAL
      : FilterReply.DENY;
  }
}
