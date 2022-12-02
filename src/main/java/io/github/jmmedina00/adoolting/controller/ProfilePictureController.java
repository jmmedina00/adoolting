package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Controller
@RequestMapping("/pfp")
public class ProfilePictureController {

  @RequestMapping(method = RequestMethod.POST, value = "/interactor/{id}")
  public String setProfilePictureOfInteractor(
    @PathVariable("id") String idStr,
    @ModelAttribute("pfp") @Valid ProfilePictureFile pfp,
    BindingResult result
  ) {
    Long interactorId;
    try {
      interactorId = Long.parseLong(idStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    if (result.hasErrors()) {
      throw new MaxUploadSizeExceededException(0); // Take advantage of exception advice
    }

    return "redirect:/home";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/group/{id}")
  public String setProfilePictureOfGroup(
    @PathVariable("id") String idStr,
    @ModelAttribute("pfp") @Valid ProfilePictureFile pfp,
    BindingResult result
  ) {
    Long groupId;
    try {
      groupId = Long.parseLong(idStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    if (result.hasErrors()) {
      throw new MaxUploadSizeExceededException(0); // Take advantage of exception advice
    }

    return "redirect:/home";
  }
}
