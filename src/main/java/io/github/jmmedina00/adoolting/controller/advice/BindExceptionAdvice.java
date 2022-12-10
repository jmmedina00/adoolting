package io.github.jmmedina00.adoolting.controller.advice;

import java.io.Serializable;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class BindExceptionAdvice {
  private static String resultPath =
    "org.springframework.validation.BindingResult.";

  @ExceptionHandler(BindException.class)
  public String returnToPreviousForm(
    BindException e,
    HttpServletRequest request,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    Map<String, Object> resultModel = result.getModel();
    String interestingKey = resultModel
      .keySet()
      .stream()
      .filter(key -> key.startsWith(resultPath))
      .findFirst()
      .get();
    String bareKey = interestingKey.replace(resultPath, "");
    Object interestingObject = resultModel.get(bareKey);

    if (interestingObject instanceof Serializable) {
      attributes.addFlashAttribute(interestingKey, result);
      attributes.addFlashAttribute(bareKey, interestingObject);
    }

    String requestingPath = request
      .getHeader("Referer")
      .replaceFirst("\\?.+$", "");

    return "redirect:" + requestingPath + "?error";
  }
}
