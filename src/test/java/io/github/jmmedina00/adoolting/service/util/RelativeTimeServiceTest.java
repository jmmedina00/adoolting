package io.github.jmmedina00.adoolting.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class RelativeTimeServiceTest {
  @MockBean
  private PersonLocaleConfigService localeConfigService;

  @Autowired
  private RelativeTimeService timeService;

  @Test
  public void getRelativeTimePreparesAndServesResultFromPrettyTimeLibrary() {
    MockedConstruction<PrettyTime> mockedTime = mockConstruction(
      PrettyTime.class,
      (mock, context) -> {
        Mockito.when(mock.format(any(Date.class))).thenReturn("Whenever");
      }
    );

    MockedStatic<LocaleContextHolder> contextUtils = Mockito.mockStatic(
      LocaleContextHolder.class
    );
    contextUtils.when(LocaleContextHolder::getLocale).thenReturn(Locale.JAPAN);

    Date date = new Date();
    String result = timeService.getRelativeTime(date);

    assertEquals("Whenever", result);
    verify(mockedTime.constructed().get(0), times(1)).setLocale(Locale.JAPAN);
    contextUtils.closeOnDemand();
    mockedTime.closeOnDemand();
  }

  @Test
  public void convertDateToCorrectTimezoneDateResultsInADateThatSubtractsPersonTimezoneFromDate() {
    long savedTimestamp = 1584026760_000L; // 2020/03/12 at 15:26 UTC
    Date resultingDate = new Date(savedTimestamp); // Gets converted to local timezone (CET -> 16:26)

    Calendar calendar = Calendar.getInstance();
    TimeZone tz = calendar.getTimeZone();

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setOffsetFromUTC(120);

    MockedStatic<AuthenticatedPerson> authPersonUtilities = Mockito.mockStatic(
      AuthenticatedPerson.class
    );
    authPersonUtilities.when(AuthenticatedPerson::getPersonId).thenReturn(4L);
    Mockito.when(localeConfigService.getConfig(4L)).thenReturn(config);

    Date converted = timeService.convertDateToCorrectTimezoneDate(
      resultingDate
    );
    authPersonUtilities.closeOnDemand();

    assertEquals(
      savedTimestamp - (120 * 60_000) - tz.getRawOffset(),
      converted.getTime()
    );
  }

  @Test
  public void getPrettyDateInCorrectTimeZoneReturnsConvertedDateAccordingToDefaultFormat() {
    long savedTimestamp = 1584026760_000L; // 2020/03/12 at 15:26 UTC
    Date resultingDate = new Date(savedTimestamp); // Gets converted to local timezone (CET -> 16:26)

    MockedStatic<LocaleContextHolder> contextUtils = Mockito.mockStatic(
      LocaleContextHolder.class
    );
    contextUtils.when(LocaleContextHolder::getLocale).thenReturn(Locale.JAPAN);

    MockedStatic<DateFormat> dateFormatUtils = Mockito.mockStatic(
      DateFormat.class
    );
    dateFormatUtils
      .when(
        () ->
          DateFormat.getDateTimeInstance(
            DateFormat.LONG,
            DateFormat.SHORT,
            Locale.JAPAN
          )
      )
      .thenReturn(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setOffsetFromUTC(120);

    MockedStatic<AuthenticatedPerson> authPersonUtilities = Mockito.mockStatic(
      AuthenticatedPerson.class
    );
    authPersonUtilities.when(AuthenticatedPerson::getPersonId).thenReturn(4L);
    Mockito.when(localeConfigService.getConfig(4L)).thenReturn(config);

    String prettyDate = timeService.getPrettyDateInCorrectTimezone(
      resultingDate
    );

    authPersonUtilities.closeOnDemand();
    dateFormatUtils.closeOnDemand();
    contextUtils.closeOnDemand();

    assertEquals("2020-03-12 13:26", prettyDate);
  }
}
