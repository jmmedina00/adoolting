package io.github.jmmedina00.adoolting.controller.advice;

import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BadPageAdvice {

  @ExceptionHandler(
    {
      NumberFormatException.class,
      NoSuchElementException.class,
      NotAuthorizedException.class,
    }
  )
  public String redirectBackToHome(Exception e) {
    return "redirect:/home?notfound";
  }

  @ExceptionHandler(TokenExpiredException.class)
  public String redirectToLanding(Exception e) {
    return "redirect:/";
  }
}
