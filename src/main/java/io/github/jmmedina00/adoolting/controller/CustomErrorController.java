package io.github.jmmedina00.adoolting.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
  @Value("${DEFAULT_IMAGE}")
  private String defaultImageFile;

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    int error = Integer.parseInt(
      request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE).toString()
    );
    if (error >= 500) {
      Exception e = (Exception) request.getAttribute(
        RequestDispatcher.ERROR_EXCEPTION
      );
      model.addAttribute("trace", getStackTrace(e));
      return "error";
    }

    // https://tomcat.apache.org/tomcat-9.0-doc/servletapi/javax/servlet/RequestDispatcher.html#FORWARD_REQUEST_URI
    String url = request
      .getAttribute("javax.servlet.forward.request_uri")
      .toString();

    if (Pattern.matches("^.+\\..+$", url)) {
      return "redirect:/cdn/" + defaultImageFile;
    }

    return "redirect:/home?notfound";
  }

  private String getStackTrace(Exception e) {
    ByteArrayOutputStream capture = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(capture);

    e.printStackTrace(writer);
    writer.close();
    return capture.toString();
  }
}
