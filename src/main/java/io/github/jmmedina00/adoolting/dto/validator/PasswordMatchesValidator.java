package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.WithConfirmablePassword;
import io.github.jmmedina00.adoolting.dto.annotation.PasswordMatches;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
  implements ConstraintValidator<PasswordMatches, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    WithConfirmablePassword passwordPair = (WithConfirmablePassword) value;
    return passwordPair.getPassword().equals(passwordPair.getConfirmPassword());
  }
}
