package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.annotation.HasMinimumAge;
import java.util.Calendar;
import java.util.Date;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MinimumAgeValidator
  implements ConstraintValidator<HasMinimumAge, Date> {
  private static int MININUM_AGE = 16;

  @Override
  public boolean isValid(Date birthday, ConstraintValidatorContext context) {
    if (birthday == null) {
      return false;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, MININUM_AGE * -1);
    Date threshold = calendar.getTime();

    return birthday.before(threshold);
  }
}
