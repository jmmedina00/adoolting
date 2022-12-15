package io.github.jmmedina00.adoolting.dto.validator;

import io.github.jmmedina00.adoolting.dto.annotation.OnlyOneKindOfMedia;
import io.github.jmmedina00.adoolting.dto.interaction.NewInteractionWithMedia;
import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OnlyOneKindOfMediaValidator
  implements ConstraintValidator<OnlyOneKindOfMedia, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    NewInteractionWithMedia newInteraction = (NewInteractionWithMedia) value;
    boolean hasFiles = newInteraction
      .getMedia()
      .stream()
      .filter(
        file -> !Optional.of(file.getOriginalFilename()).orElse("").isEmpty()
      )
      .findFirst()
      .isPresent();
    boolean isUrlPopulated = !newInteraction.getUrl().isEmpty();
    return (hasFiles ^ isUrlPopulated) || (!hasFiles && !isUrlPopulated);
  }
}
