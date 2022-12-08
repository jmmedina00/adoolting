package io.github.jmmedina00.adoolting.controller.interaction;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import io.github.jmmedina00.adoolting.service.interaction.ProfilePictureService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Controller
@RequestMapping("/pfp")
public class ProfilePictureController {
  @Autowired
  private ProfilePictureService profilePictureService;

  @Value("${DEFAULT_IMAGE}")
  private String defaultImageFile;

  @RequestMapping(method = RequestMethod.GET, value = "/interactor/{id}")
  public String getProfilePictureOfInteractor(
    @PathVariable("id") String idStr,
    @RequestParam(required = false, name = "size") String size
  ) {
    try {
      Long interactorId = Long.parseLong(idStr);
      ProfilePicture pfp = profilePictureService.getProfilePictureOfInteractor(
        interactorId
      );
      return (
        "redirect:/media/thumbnail/" +
        (size == null ? 64 : size) +
        "/" +
        pfp.getId()
      );
    } catch (Exception e) {
      return "redirect:/cdn/" + defaultImageFile;
    }
  }

  @RequestMapping(method = RequestMethod.GET, value = "/group/{id}")
  public String getProfilePictureOfGroup(
    @PathVariable("id") String idStr,
    @RequestParam(required = false, name = "size") String size
  ) {
    try {
      Long groupId = Long.parseLong(idStr);
      ProfilePicture pfp = profilePictureService.getProfilePictureOfGroup(
        groupId
      );
      return (
        "redirect:/media/thumbnail/" +
        (size == null ? 64 : size) +
        "/" +
        pfp.getId()
      );
    } catch (Exception e) {
      return "redirect:/cdn/default";
    }
  }

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

    try {
      ProfilePicture saved = profilePictureService.setProfilePictureOfInteractor(
        interactorId,
        AuthenticatedPerson.getPersonId(),
        pfp
      );
      return ("redirect:/interaction/" + saved.getInteraction().getId());
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }
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

    try {
      profilePictureService.setProfilePictureOfGroup(
        groupId,
        AuthenticatedPerson.getPersonId(),
        pfp
      );
      return "redirect:/interaction/" + groupId;
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }
  }
}
