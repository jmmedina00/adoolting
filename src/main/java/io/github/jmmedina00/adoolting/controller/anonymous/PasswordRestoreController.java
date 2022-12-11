package io.github.jmmedina00.adoolting.controller.anonymous;

import io.github.jmmedina00.adoolting.dto.util.ForgotPassword;
import io.github.jmmedina00.adoolting.dto.util.RestorePassword;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import io.github.jmmedina00.adoolting.service.util.PasswordRestoreService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/restore-password")
public class PasswordRestoreController {
  @Autowired
  PasswordRestoreService restoreService;

  @RequestMapping(method = RequestMethod.GET)
  public String getSendLinkForm(Model model) {
    if (!model.containsAttribute("forgotPassword")) {
      ForgotPassword forgotPassword = new ForgotPassword();
      model.addAttribute("forgotPassword", forgotPassword);
    }

    return "password/forgot-password";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String sendRestoreLink(
    @ModelAttribute("forgotPassword") @Valid ForgotPassword forgotPassword
  ) {
    try {
      restoreService.createTokenFromEmail(forgotPassword.getEmail());
    } catch (UsernameNotFoundException e) {
      System.out.println(e.getMessage());
    }

    return "password/email-sent";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{token}")
  public String getRestoreForm(
    @PathVariable("token") String token,
    Model model
  )
    throws TokenExpiredException {
    if (!model.containsAttribute("newPassword")) {
      RestorePassword restorePassword = new RestorePassword();
      model.addAttribute("newPassword", restorePassword);
    }

    model.addAttribute("token", restoreService.getToken(token));
    return "password/restore-password";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{token}")
  public String restorePassword(
    @PathVariable("token") String token,
    @ModelAttribute("newPassword") @Valid RestorePassword restorePassword
  )
    throws TokenExpiredException {
    restoreService.changePasswordWithToken(
      token,
      restorePassword.getPassword()
    );
    return "redirect:/?restored";
  }
}
