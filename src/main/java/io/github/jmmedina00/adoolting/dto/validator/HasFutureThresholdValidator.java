package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.annotation.HasFutureThreshold;
import io.github.jmmedina00.adoolting.dto.group.NewEvent;
import java.util.Calendar;
import java.util.Date;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasFutureThresholdValidator
  implements ConstraintValidator<HasFutureThreshold, Object> {
  private static int MINIMUM_HOURS = 2;

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    Calendar calendarThreshold = Calendar.getInstance();
    calendarThreshold.add(Calendar.HOUR_OF_DAY, MINIMUM_HOURS);
    Date date = ((NewEvent) value).getFinalizedDate();

    if (calendarThreshold.getTime().before(date)) {
      return true;
    }

    context.disableDefaultConstraintViolation();
    context
      .buildConstraintViolationWithTemplate("{error.datetime}")
      .addPropertyNode("date")
      .addConstraintViolation();
    context
      .buildConstraintViolationWithTemplate("{error.datetime}")
      .addPropertyNode("time")
      .addConstraintViolation();
    return false;
  }
}
