package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PasswordMatchesValidatorTest {
  PasswordMatchesValidator validator = new PasswordMatchesValidator();

  @Test
  public void isValidReturnsTrueWhenBothPasswordAndConfirmPasswordAreEqual() {
    SecureDeletion deletion = new SecureDeletion();

    deletion.setPassword("myPassword");
    deletion.setConfirmPassword("myPassword");

    assertTrue(validator.isValid(deletion, null));
  }

  @Test
  public void isValidReturnsFalseAndsesCustomViolationWhenPasswordNotEqualsConfirmPassword() {
    SecureDeletion deletion = new SecureDeletion();

    deletion.setPassword("myPassword");
    deletion.setConfirmPassword("notMyPassword");

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
      .when(
        context.buildConstraintViolationWithTemplate("{error.password.confirm}")
      )
      .thenReturn(builder);
    Mockito
      .when(builder.addPropertyNode("confirmPassword"))
      .thenReturn(nodeBuilder);

    assertFalse(validator.isValid(deletion, context));
  }
}
