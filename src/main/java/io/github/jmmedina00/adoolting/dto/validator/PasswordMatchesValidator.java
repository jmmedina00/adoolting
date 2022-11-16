package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.dto.annotation.PasswordMatches;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
  implements ConstraintValidator<PasswordMatches, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    User userDto = (User) value;
    return userDto.getPassword().equals(userDto.getConfirmPassword());
  }
}
