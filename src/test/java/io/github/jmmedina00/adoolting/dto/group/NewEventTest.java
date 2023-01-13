package io.github.jmmedina00.adoolting.dto.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.jmmedina00.adoolting.dto.common.DateExtractOfDate;
import io.github.jmmedina00.adoolting.dto.common.TimeExtractOfDate;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class NewEventTest {

  @Test
  public void getFinalizedDateProvidesDateThatTranslateDateAndTimeExtractIntoCorrectTimestamp() {
    int offset = 120;

    Calendar calendarForTime = Calendar.getInstance();
    calendarForTime.setTimeInMillis(0); // Reset every other value in calendar
    calendarForTime.set(Calendar.HOUR, 12);
    calendarForTime.set(Calendar.MINUTE, 40);

    Date timeExtractValue = calendarForTime.getTime();

    Calendar calendarForDate = Calendar.getInstance();
    calendarForDate.setTimeInMillis(0);
    calendarForDate.set(Calendar.YEAR, 2021);
    calendarForDate.set(Calendar.MONTH, Calendar.APRIL);
    calendarForDate.set(Calendar.DAY_OF_MONTH, 12);

    Date dateExtractValue = calendarForDate.getTime();

    long expected = 1618238400_000L; // 2021-04-12 at 14:40 UTC

    NewEvent event = new NewEvent();
    event.setOffsetFromUTC(offset);
    event.setDate(new DateExtractOfDate(dateExtractValue));
    event.setTime(new TimeExtractOfDate(timeExtractValue));

    assertEquals(expected, event.getFinalizedDate().getTime());
  }

  @Test
  public void getFinalizedDateProvidesMaxDateIfDateNotPopulated() {
    NewEvent event = new NewEvent();
    event.setOffsetFromUTC(0);
    event.setDate(new DateExtractOfDate("nonsense")); // Should come up bad
    event.setTime(new TimeExtractOfDate(new Date()));

    assertEquals(new Date(Long.MAX_VALUE), event.getFinalizedDate());
  }

  @Test
  public void getFinalizedDateProvidesMaxDateIfTimeNotPopulated() {
    NewEvent event = new NewEvent();
    event.setOffsetFromUTC(0);
    event.setDate(new DateExtractOfDate(new Date()));
    event.setTime(new TimeExtractOfDate("nonsense")); // Should come up bad

    assertEquals(new Date(Long.MAX_VALUE), event.getFinalizedDate());
  }
}
