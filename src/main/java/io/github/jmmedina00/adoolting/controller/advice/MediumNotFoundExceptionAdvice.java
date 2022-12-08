package io.github.jmmedina00.adoolting.controller.advice;

import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MediumNotFoundExceptionAdvice {
  @Value("${DEFAULT_IMAGE}")
  private String defaultImageFile;

  @ExceptionHandler(MediumNotFoundException.class)
  public String redirectToDefaultImage(Exception e) {
    return "redirect:/cdn/" + defaultImageFile;
  }
}
