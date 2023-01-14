package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.dto.common.DateExtractOfDate;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class DatePortionIsNotEmptyValidatorTest {
  DatePortionIsNotEmptyValidator validator = new DatePortionIsNotEmptyValidator();

  @Test
  public void isValidReturnsTrueWhenPortionHasANonNullValue() {
    DateExtractOfDate date = new DateExtractOfDate(new Date());
    assertTrue(validator.isValid(date, null));
  }

  @Test
  public void isValidReturnsFalseWhenPortionHasNullValue() {
    DateExtractOfDate date = new DateExtractOfDate("This translates to null");
    assertFalse(validator.isValid(date, null));
  }
}
