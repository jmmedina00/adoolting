package io.github.jmmedina00.adoolting.service.util;

import java.util.Date;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RelativeTimeService {
  private PrettyTime prettyTime = new PrettyTime();

  public String getRelativeTime(Date date) {
    prettyTime.setLocale(LocaleContextHolder.getLocale());
    return prettyTime.format(date);
  }
}
