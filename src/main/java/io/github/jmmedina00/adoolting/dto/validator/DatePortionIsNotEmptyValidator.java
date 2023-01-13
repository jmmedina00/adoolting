package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.common.DatePortion;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

public class DatePortionIsNotEmptyValidator
  implements ConstraintValidator<NotEmpty, DatePortion> {

  @Override
  public boolean isValid(
    DatePortion portion,
    ConstraintValidatorContext context
  ) {
    return portion.getValue() != null;
  }
}
