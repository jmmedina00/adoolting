package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyImages;
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
            Optional.of(file.getContentType()).orElse("").matches("^image/.+$")
          )
      )
      .count();

    System.out.println(invalidFiles);
    return invalidFiles == 0;
  }
}
