package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.NewConfirmableInteraction;
import io.github.jmmedina00.adoolting.dto.NewPost;
import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.entity.Post;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.PersonService;
import io.github.jmmedina00.adoolting.service.PostService;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
  private PostService postService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @RequestMapping(method = RequestMethod.GET)
  public String redirectToAuthenticatedPersonProfile() {
    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    if (authentication instanceof AnonymousAuthenticationToken) {
      return "redirect:/";
    }

    Long personId =
      ((PersonDetails) authentication.getPrincipal()).getPerson().getId();

    return "redirect:/profile/" + personId;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{personId}")
  public String getPersonProfile(
    @PathVariable("personId") String personIdStr,
    Model model
  ) {
    Long personId;

    try {
      personId = Long.parseLong(personIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();
    Person person = personService.getPerson(personId);

    if (person == null) {
      return "redirect:/home?notfound";
    }

    PersonDetails details = new PersonDetails(person);
    if (!details.isEnabled()) {
      return "redirect:/home?notfound";
    }

    NewConfirmableInteraction cInteraction = new NewConfirmableInteraction();

    if (!Objects.equals(authenticatedPerson.getId(), person.getId())) {
      cInteraction.setPersonId(personId);
    }

    model.addAttribute("person", person);
    model.addAttribute(
      "friendship",
      cInteractionService.getPersonFriendship(authenticatedPerson, person)
    );
    model.addAttribute("friends", cInteractionService.getPersonFriends(person));
    model.addAttribute(
      "posts",
      interactionService.getInteractions(person.getId())
    );
    model.addAttribute("newPost", new NewPost());
    model.addAttribute("cInteraction", cInteraction);

    return "profile";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{personId}")
  public String commentOnPersonProfile(
    @PathVariable("personId") String personIdStr,
    @ModelAttribute("newPost") @Valid NewPost newPost,
    BindingResult result
  ) {
    Long personId;

    try {
      personId = Long.parseLong(personIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    if (result.hasErrors()) {
      return "redirect:/profile/" + personId + "?error";
    }

    Person personFromProfile = personService.getPerson(personId);
    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Post savedPost;

    if (
      Objects.equals(personFromProfile.getId(), authenticatedPerson.getId())
    ) {
      savedPost = postService.createPost(authenticatedPerson, newPost);
    } else {
      savedPost =
        postService.postOnProfile(
          authenticatedPerson,
          personFromProfile,
          newPost
        );
    }

    return "redirect:/profile/" + personId + "?post=" + savedPost.getId();
  }
}
