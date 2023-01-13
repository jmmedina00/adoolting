package io.github.jmmedina00.adoolting.dto.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
public class DateExtractOfDateTest {

  @Test
  public void constructorParsesYearMonthAndDayIntoADate() {
    String date = "2022-11-12";

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(0);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.YEAR, 2022);
    calendar.set(Calendar.MONTH, Calendar.NOVEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 12);

    Date expected = calendar.getTime();
    DateExtractOfDate extract = new DateExtractOfDate(date);

    assertEquals(expected, extract.getValue());
  }

  @Test
  public void toStringProvidesDateFormattedBackIntoYearMonthAndDay() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(0);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.YEAR, 2024);
    calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 29);

    Date date = calendar.getTime();
    DateExtractOfDate extract = new DateExtractOfDate(date);
    assertEquals("2024-09-29", extract.toString());
  }
}
