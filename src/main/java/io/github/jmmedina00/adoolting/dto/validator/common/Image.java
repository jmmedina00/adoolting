package io.github.jmmedina00.adoolting.dto.validator.common;

import java.util.List;
import java.util.Optional;

public class Image {

  public static boolean isValidImage(Optional<String> mimeType) {
    return List.of("image/jpeg", "image/png").indexOf(mimeType.orElse("")) >= 0;
  }
}
