package io.github.jmmedina00.adoolting.dto.annotation;

import io.github.jmmedina00.adoolting.dto.validator.OnlyImagesValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OnlyImagesValidator.class)
@Documented
public @interface OnlyImages {
  String message() default "Only images allowed";

  Class<?>[] groups() default {  };

  Class<? extends Payload>[] payload() default {  };
}
