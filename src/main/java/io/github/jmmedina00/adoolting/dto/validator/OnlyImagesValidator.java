package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImages;
import io.github.jmmedina00.adoolting.dto.validator.common.Image;
import java.util.List;
import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class OnlyImagesValidator
  implements ConstraintValidator<OnlyImages, List<MultipartFile>> {

  @Override
  public boolean isValid(
    List<MultipartFile> files,
    ConstraintValidatorContext context
  ) {
    if (files.size() > 6) {
      return false;
    }

    Long invalidFiles = files
      .stream()
      .filter(
        file ->
          !(
            Optional.of(file.getOriginalFilename()).orElse("").isEmpty() ||
            Image.isValidImage(Optional.of(file.getContentType()))
          )
      )
      .count();

    return invalidFiles == 0;
  }
}
