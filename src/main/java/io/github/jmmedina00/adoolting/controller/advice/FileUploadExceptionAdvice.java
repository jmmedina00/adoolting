package io.github.jmmedina00.adoolting.controller.advice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class FileUploadExceptionAdvice {

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public String handleMaxSizeException(
    MaxUploadSizeExceededException e,
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    String requestingPath = request
      .getHeader("Referer")
      .replaceFirst("\\?.+$", ""); // Any better way to address going back to previous URL?

    return "redirect:" + requestingPath + "?error";
  }
}
