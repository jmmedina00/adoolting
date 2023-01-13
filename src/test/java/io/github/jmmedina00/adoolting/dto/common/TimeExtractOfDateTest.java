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
public class TimeExtractOfDateTest {

  @Test
  public void constructorParsesTwentyFourHourTimeIntoADate() {
    String time = "20:08";

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(0);
    calendar.set(Calendar.HOUR_OF_DAY, 20);
    calendar.set(Calendar.MINUTE, 8);

    Date expected = calendar.getTime();
    TimeExtractOfDate extract = new TimeExtractOfDate(time);

    assertEquals(expected, extract.getValue());
  }

  @Test
  public void toStringProvidesTimeInTwentyFourHourFormat() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 13);
    calendar.set(Calendar.MINUTE, 44);

    Date date = calendar.getTime();
    TimeExtractOfDate extract = new TimeExtractOfDate(date);

    assertEquals("13:44", extract.toString());
  }
}
