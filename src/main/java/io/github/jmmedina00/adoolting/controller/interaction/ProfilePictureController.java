package io.github.jmmedina00.adoolting.controller.interaction;

import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.ProfilePicture;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.interaction.ProfilePictureService;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private InteractorService interactorService;

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

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Interactor interactor = interactorService.getInteractor(interactorId);

    if (
      interactor instanceof Person &&
      !Objects.equals(interactor.getId(), authenticatedPerson.getId())
    ) {
      return "redirect:/home?notfound";
    }

    if (interactor instanceof Page) {
      Page page = (Page) interactor;
      if (
        !Objects.equals(
          page.getCreatedByPerson().getId(),
          authenticatedPerson.getId()
        )
      ) {
        return "redirect:/home?notfound";
      }
    }

    profilePictureService.setProfilePictureOfInteractor(interactor, pfp);

    return (
      "redirect:/" +
      ((interactor instanceof Page) ? ("page/" + interactorId) : "profile")
    );
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

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Interaction interaction;
    try {
      interaction = interactionService.getInteraction(groupId);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    if (!(interaction instanceof PeopleGroup)) {
      System.out.println("Not a group");
      return "redirect:/home?notfound";
    }

    PeopleGroup group = (PeopleGroup) interaction;
    Interactor owner = group.getInteractor();

    if (
      owner instanceof Person &&
      !Objects.equals(owner.getId(), authenticatedPerson.getId())
    ) {
      return "redirect:/home?notfound";
    }

    if (owner instanceof Page) {
      Page page = (Page) owner;

      if (
        !Objects.equals(
          page.getCreatedByPerson().getId(),
          authenticatedPerson.getId()
        )
      ) {
        return "redirect:/home?notfound";
      }
    }

    profilePictureService.setProfilePictureOfGroup(
      group,
      authenticatedPerson,
      pfp
    );
    return "redirect:/interaction/" + groupId;
  }
}
