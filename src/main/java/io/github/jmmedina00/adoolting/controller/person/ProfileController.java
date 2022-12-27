package io.github.jmmedina00.adoolting.controller.person;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.NewConfirmableInteraction;
import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.group.PeopleGroupService;
import io.github.jmmedina00.adoolting.service.interaction.PostService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.service.person.PersonSettingsService;
import io.github.jmmedina00.adoolting.service.person.PersonStatusService;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/profile")
public class ProfileController {
  @Autowired
  private PersonService personService;

  @Autowired
  private PersonSettingsService settingsService;

  @Autowired
  private PersonStatusService statusService;

  @Autowired
  private PostService postService;

  @Autowired
  private PeopleGroupService groupService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @RequestMapping(method = RequestMethod.GET)
  public String redirectToAuthenticatedPersonProfile() {
    return "redirect:/profile/" + AuthenticatedPerson.getPersonId();
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{personId}")
  public String getPersonProfile(
    @PathVariable("personId") Long personId,
    @PageableDefault(value = 10, page = 0) Pageable pageable,
    Model model
  ) {
    Long authenticatedPersonId = AuthenticatedPerson.getPersonId();
    Person person = personService.getPerson(personId);
    NewConfirmableInteraction cInteraction = new NewConfirmableInteraction();

    if (!Objects.equals(authenticatedPersonId, personId)) {
      cInteraction.setPersonId(personId);
    }

    ConfirmableInteraction friendship = cInteractionService.getPersonFriendship(
      authenticatedPersonId,
      personId
    );

    model.addAttribute("person", person);
    model.addAttribute("friendship", friendship);
    model.addAttribute("cInteraction", cInteraction);
    model.addAttribute(
      "notAllowedByPerson",
      !Objects.equals(personId, authenticatedPersonId) &&
      (friendship == null || friendship.getConfirmedAt() == null) &&
      !settingsService.isAllowedByPerson(
        personId,
        PersonSettingsService.ENTER_PROFILE
      )
    );
    model.addAttribute("status", statusService.getPersonStatus(personId));
    model.addAttribute(
      "groups",
      groupService.getGroupsManagedByPerson(authenticatedPersonId)
    );
    model.addAttribute(
      "friends",
      cInteractionService.getPersonFriends(personId)
    );
    model.addAttribute(
      "posts",
      interactionService.getInteractions(person.getId(), pageable)
    );
    model.addAttribute("newPost", new NewPost());

    return "person/profile/show";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{personId}")
  public String commentOnPersonProfile(
    @PathVariable("personId") Long personId,
    @ModelAttribute("newPost") @Valid NewPost newPost
  )
    throws NotAuthorizedException {
    Post savedPost = postService.postOnProfile(
      AuthenticatedPerson.getPersonId(),
      personId,
      newPost
    );
    return "redirect:/profile/" + personId + "?post=" + savedPost.getId();
  }
}
