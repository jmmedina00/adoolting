package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.ForgotPassword;
import io.github.jmmedina00.adoolting.dto.RestorePassword;
import io.github.jmmedina00.adoolting.service.PasswordRestoreService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    return "forgot-password";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String sendRestoreLink(
    @ModelAttribute("forgotPassword") @Valid ForgotPassword forgotPassword,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.forgotPassword",
        result
      );
      attributes.addFlashAttribute("forgotPassword", forgotPassword);
      return "redirect:restore-password";
    }

    try {
      restoreService.createTokenFromEmail(forgotPassword.getEmail());
    } catch (UsernameNotFoundException e) {
      System.out.println(e.getMessage());
    }

    return "email-sent";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{token}")
  public String getRestoreForm(
    @PathVariable("token") String token,
    Model model
  ) {
    if (!restoreService.isTokenStillUseful(token)) {
      return "redirect:/";
    }

    if (!model.containsAttribute("newPassword")) {
      RestorePassword restorePassword = new RestorePassword();
      model.addAttribute("newPassword", restorePassword);
    }

    model.addAttribute("token", token);

    return "restore-password";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{token}")
  public String restorePassword(
    @PathVariable("token") String token,
    @ModelAttribute("newPassword") @Valid RestorePassword restorePassword,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    if (!restoreService.isTokenStillUseful(token)) {
      return "redirect:/";
    }

    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.newPassword",
        result
      );
      attributes.addFlashAttribute("newPassword", restorePassword);
      return "redirect:/restore-password/" + token;
    }

    restoreService.changePasswordWithToken(
      token,
      restorePassword.getPassword()
    );
    return "redirect:/?restored";
  }
}
