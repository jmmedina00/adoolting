package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.NewComment;
import io.github.jmmedina00.adoolting.entity.Comment;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.CommentService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/interaction")
public class InteractionController {
  @Autowired
  private InteractionService interactionService;

  @Autowired
  private CommentService commentService;

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  public String getInteraction(
    @PathVariable("id") String interactionIdStr,
    Model model
  ) {
    Long interactionId;
    Interaction interaction;
    try {
      interactionId = Long.parseLong(interactionIdStr);
      interaction = interactionService.getInteraction(interactionId);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    model.addAttribute("interaction", interaction);
    model.addAttribute(
      "comments",
      commentService.getCommentsFromInteraction(interactionId)
    );
    model.addAttribute("newComment", new NewComment());

    return "interaction";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}/comment")
  public String commentOnInteraction(
    @PathVariable("id") String interactionIdStr,
    @ModelAttribute("newComment") @Valid NewComment newComment,
    BindingResult result
  ) {
    Long interactionId;
    try {
      interactionId = Long.parseLong(interactionIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    if (result.hasErrors()) {
      return "redirect:/interaction/" + interactionId + "?error";
    }

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Comment comment = commentService.createComment(
      newComment,
      authenticatedPerson,
      interactionId
    );
    return (
      "redirect:/interaction/" + interactionId + "?comment=" + comment.getId()
    );
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
  public String deleteInteraction(@PathVariable("id") String interactionIdStr) {
    Long interactionId;
    try {
      interactionId = Long.parseLong(interactionIdStr);
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

    try {
      interactionService.deleteInteraction(interactionId, authenticatedPerson);
    } catch (NotAuthorizedException e) {
      return "redirect:/home?notfound";
    }

    return "redirect:/profile/" + authenticatedPerson.getId() + "?success";
  }
}
