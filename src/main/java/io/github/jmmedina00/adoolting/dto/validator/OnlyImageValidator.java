package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImage;
import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class OnlyImageValidator
  implements ConstraintValidator<OnlyImage, MultipartFile> {

  @Override
  public boolean isValid(
    MultipartFile file,
    ConstraintValidatorContext context
  ) {
    return (
      Optional.of(file.getOriginalFilename()).orElse("").isEmpty() ||
      Optional.of(file.getContentType()).orElse("").matches("^image/.+$")
    );
  }
}
