package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.dto.annotation.EmailMatches;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailMatchesValidator
  implements ConstraintValidator<EmailMatches, Object> {

  // https://stackoverflow.com/questions/7109296/bind-global-errors-generated-from-form-validation-to-specific-form-fields-in-spr

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    User userDto = (User) value;
    if (userDto.getEmail().equals(userDto.getConfirmEmail())) {
      return true;
    }

    context.disableDefaultConstraintViolation();
    context
      .buildConstraintViolationWithTemplate("{error.email.confirm}")
      .addPropertyNode("confirmEmail")
      .addConstraintViolation();
    return false;
  }
}
