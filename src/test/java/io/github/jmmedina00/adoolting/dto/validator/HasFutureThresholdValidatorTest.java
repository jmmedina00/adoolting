package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Calendar;
import java.util.Date;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class HasFutureThresholdValidatorTest {
  HasFutureThresholdValidator validator = new HasFutureThresholdValidator();

  int MINIMUM_HOURS = 2;

  @Test
  public void isValidReturnsTrueIfTheDateTimeReceivedIsBeforeEstablishedThreshold() {
    Calendar validCalendar = Calendar.getInstance();
    validCalendar.add(Calendar.HOUR_OF_DAY, 4);
    Date validDate = validCalendar.getTime();

    NewEvent event = Mockito.mock(NewEvent.class);
    Mockito.when(event.getFinalizedDate()).thenReturn(validDate);

    assertTrue(validator.isValid(event, null));
  }

  @Test
  public void isValidReturnsFalseAndUsesCustomViolationIfTheTimeReceivedIsBeforeTheThreshold() {
    Calendar invalidCalendar = Calendar.getInstance();
    invalidCalendar.add(Calendar.HOUR_OF_DAY, -12);
    Date invalidDate = invalidCalendar.getTime();

    NewEvent event = Mockito.mock(NewEvent.class);
    Mockito.when(event.getFinalizedDate()).thenReturn(invalidDate);

    ConstraintValidatorContext context = Mockito.mock(
      ConstraintValidatorContext.class
    );
    ConstraintViolationBuilder builder = Mockito.mock(
      ConstraintViolationBuilder.class
    );
    NodeBuilderCustomizableContext nodeBuilder = Mockito.mock(
      NodeBuilderCustomizableContext.class
    );

    Mockito
      .when(context.buildConstraintViolationWithTemplate(anyString()))
      .thenReturn(builder);
    Mockito.when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

    assertFalse(validator.isValid(event, context));

    verify(context, times(2))
      .buildConstraintViolationWithTemplate("{error.datetime}");
    verify(builder, times(1)).addPropertyNode("date");
    verify(builder, times(1)).addPropertyNode("time");
    verify(nodeBuilder, times(2)).addConstraintViolation();
  }
}
