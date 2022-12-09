package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.group.JoinRequestService;
import io.github.jmmedina00.adoolting.service.interaction.CommentService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

  @Autowired
  private JoinRequestService joinRequestService;

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  public String getInteraction(
    @PathVariable("id") Long interactionId,
    @PageableDefault(value = 10, page = 0) Pageable pageable,
    Model model
  ) {
    Interaction interaction = interactionService.getInteraction(interactionId);

    if (interaction instanceof Comment) {
      Comment comment = (Comment) interaction;

      if (!(comment.getReceiverInteraction() instanceof PeopleGroup)) {
        return (
          "redirect:/interaction/" +
          comment.getReceiverInteraction().getId() +
          "?comment=" +
          comment.getId()
        );
      }
    }

    model.addAttribute("interaction", interaction);
    model.addAttribute(
      "comments",
      commentService.getCommentsFromInteraction(interactionId, pageable)
    );
    model.addAttribute("newComment", new NewComment());
    if (interaction instanceof PeopleGroup) {
      model.addAttribute("groupPfp", "/pfp/group/" + interactionId);
      model.addAttribute(
        "joinRequest",
        joinRequestService.getJoinRequestForPersonAndGroup(
          AuthenticatedPerson.getPersonId(),
          interactionId
        )
      );
    }

    return "interaction";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}/comment")
  public String commentOnInteraction(
    @PathVariable("id") Long interactionId,
    @ModelAttribute("newComment") @Valid NewComment newComment,
    BindingResult result
  ) {
    if (result.hasErrors()) {
      return "redirect:/interaction/" + interactionId + "?error";
    }

    Comment comment = commentService.createComment(
      newComment,
      AuthenticatedPerson.getPersonId(),
      interactionId
    );
    return (
      "redirect:/interaction/" + interactionId + "?comment=" + comment.getId()
    );
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
  public String deleteInteraction(@PathVariable("id") Long interactionId)
    throws NotAuthorizedException {
    Long personId = AuthenticatedPerson.getPersonId();
    interactionService.deleteInteraction(interactionId, personId);
    return "redirect:/profile/" + personId + "?success";
  }
}
