package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImageRequired;
import io.github.jmmedina00.adoolting.dto.validator.common.Image;
import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class OnlyImageRequiredValidator
  implements ConstraintValidator<OnlyImageRequired, MultipartFile> {

  @Override
  public boolean isValid(
    MultipartFile file,
    ConstraintValidatorContext context
  ) {
    return (Image.isValidImage(Optional.of(file.getContentType())));
  }
}
