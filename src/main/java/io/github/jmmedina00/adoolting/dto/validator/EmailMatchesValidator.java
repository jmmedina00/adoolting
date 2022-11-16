package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.dto.annotation.EmailMatches;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailMatchesValidator
  implements ConstraintValidator<EmailMatches, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    User userDto = (User) value;
    return userDto.getEmail().equals(userDto.getConfirmEmail());
  }
}
