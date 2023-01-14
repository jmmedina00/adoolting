package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.dto.common.DateExtractOfDate;
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
public class MinimumAgeValidatorTest {
  MinimumAgeValidator validator = new MinimumAgeValidator();

  int MININUM_AGE = 16;

  @Test
  public void isValidReturnsTrueWhenDateIsBeforeCurrentDateMinusMinimumYears() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, -20);

    Date birthday = calendar.getTime();

    assertTrue(validator.isValid(new DateExtractOfDate(birthday), null));
  }

  @Test
  public void isValidReturnsFalseWhenDateIsAfterCurrentDateMinusMinimumYears() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, -12);

    Date birthday = calendar.getTime();

    assertFalse(validator.isValid(new DateExtractOfDate(birthday), null));
  }

  @Test
  public void isValidReturnsFalseWhenProvidedWithEmptyOrNonValidDate() {
    assertFalse(
      validator.isValid(new DateExtractOfDate("This is nonsense"), null)
    );
  }
}
