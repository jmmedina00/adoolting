package io.github.jmmedina00.adoolting.service.util;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RelativeTimeService {
  @Autowired
  private PersonLocaleConfigService localeConfigService;

  public String getRelativeTime(Date date) {
    PrettyTime prettyTime = new PrettyTime();
    prettyTime.setLocale(LocaleContextHolder.getLocale());
    return prettyTime.format(date);
  }

  public String getPrettyDateInCorrectTimezone(Date date) {
    DateFormat format = DateFormat.getDateTimeInstance(
      DateFormat.LONG,
      DateFormat.SHORT,
      LocaleContextHolder.getLocale()
    );
    Date converted = convertDateToCorrectTimezoneDate(date);
    return format.format(converted);
  }

  public Date convertDateToCorrectTimezoneDate(Date date) {
    PersonLocaleConfig locale = localeConfigService.getConfig(
      AuthenticatedPerson.getPersonId()
    );

    Calendar happeningAtCalendar = Calendar.getInstance();
    TimeZone tz = happeningAtCalendar.getTimeZone();
    happeningAtCalendar.setTime(date);
    happeningAtCalendar.add(Calendar.MILLISECOND, tz.getRawOffset() * -1);
    happeningAtCalendar.add(Calendar.MINUTE, locale.getOffsetFromUTC() * -1);

    return happeningAtCalendar.getTime();
  }
}
