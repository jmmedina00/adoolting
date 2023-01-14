package io.github.jmmedina00.adoolting.dto.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
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
public class EmailMatchesValidatorTest {
  EmailMatchesValidator validator = new EmailMatchesValidator();

  @Test
  public void isValidReturnsTrueWhenUserEmailAndConfirmEmailMatches() {
    User user = new User();
    user.setEmail("test@test.local");
    user.setConfirmEmail("test@test.local");

    assertTrue(validator.isValid(user, null));
  }

  @Test
  public void isValidReturnsFalseAndUsesCustomViolationWhenEmailsInputNotMatching() {
    User user = new User();
    user.setEmail("test@test.local");
    user.setConfirmEmail("none@test.local");

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
        context.buildConstraintViolationWithTemplate("{error.email.confirm}")
      )
      .thenReturn(builder);
    Mockito.when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);

    assertFalse(validator.isValid(user, context));

    verify(builder, times(1)).addPropertyNode("confirmEmail");
    verify(nodeBuilder, times(1)).addConstraintViolation();
  }
}
