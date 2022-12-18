package io.github.jmmedina00.adoolting.controller.interaction;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import io.github.jmmedina00.adoolting.exception.MediumNotFoundException;
import io.github.jmmedina00.adoolting.service.interaction.ProfilePictureService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/pfp")
public class ProfilePictureController {
  @Autowired
  private ProfilePictureService profilePictureService;

  @RequestMapping(method = RequestMethod.GET, value = "/interactor/{id}")
  public String getProfilePictureOfInteractor(
    @PathVariable("id") Long interactorId,
    @RequestParam(required = false, name = "size") String size
  )
    throws MediumNotFoundException {
    ProfilePicture pfp = profilePictureService.getProfilePictureOfInteractor(
      interactorId
    );
    return provideFinalThumbnail(pfp, size);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/group/{id}")
  public String getProfilePictureOfGroup(
    @PathVariable("id") Long groupId,
    @RequestParam(required = false, name = "size") String size
  )
    throws MediumNotFoundException {
    ProfilePicture pfp = profilePictureService.getProfilePictureOfGroup(
      groupId
    );
    return provideFinalThumbnail(pfp, size);
  }

  @RequestMapping(method = RequestMethod.POST, value = "/interactor/{id}")
  public String setProfilePictureOfInteractor(
    @PathVariable("id") Long interactorId,
    @ModelAttribute("pfp") @Valid ProfilePictureFile pfp
  )
    throws Exception {
    ProfilePicture saved = profilePictureService.setProfilePictureOfInteractor(
      interactorId,
      AuthenticatedPerson.getPersonId(),
      pfp
    );
    return ("redirect:/interaction/" + saved.getInteraction().getId());
  }

  @RequestMapping(method = RequestMethod.POST, value = "/group/{id}")
  public String setProfilePictureOfGroup(
    @PathVariable("id") Long groupId,
    @ModelAttribute("pfp") @Valid ProfilePictureFile pfp
  )
    throws Exception {
    profilePictureService.setProfilePictureOfGroup(
      groupId,
      AuthenticatedPerson.getPersonId(),
      pfp
    );
    return "redirect:/interaction/" + groupId;
  }

  private String provideFinalThumbnail(ProfilePicture pfp, String sizeStr) {
    return (
      "redirect:/media/thumbnail/" +
      (sizeStr == null ? 64 : sizeStr) +
      "/" +
      pfp.getId()
    );
  }
}
