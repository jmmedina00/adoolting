package io.github.jmmedina00.adoolting.dto.annotation;

import io.github.jmmedina00.adoolting.dto.validator.EmailMatchesValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailMatchesValidator.class)
@Documented
public @interface EmailMatches {
  String message() default "Emails don't match";

  Class<?>[] groups() default {  };

  Class<? extends Payload>[] payload() default {  };
}
