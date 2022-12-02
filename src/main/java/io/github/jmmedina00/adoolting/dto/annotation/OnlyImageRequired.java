package io.github.jmmedina00.adoolting.dto.annotation;

import io.github.jmmedina00.adoolting.dto.validator.OnlyImageRequiredValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OnlyImageRequiredValidator.class)
public @interface OnlyImageRequired {
  String message() default "Only images allowed";

  Class<?>[] groups() default {  };

  Class<? extends Payload>[] payload() default {  };
}
