package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.interaction.NewComment;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.enums.AccessLevel;
import io.github.jmmedina00.adoolting.entity.group.PeopleGroup;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.group.JoinRequestService;
import io.github.jmmedina00.adoolting.service.interaction.CommentService;
import io.github.jmmedina00.adoolting.service.person.PersonAccessLevelService;
import javax.validation.Valid;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/interaction")
public class InteractionController {
  @Autowired
  private InteractionService interactionService;

  @Autowired
  private CommentService commentService;

  @Autowired
  private JoinRequestService joinRequestService;

  @Autowired
  private PersonAccessLevelService accessLevelService;

  private static final Logger logger = LoggerFactory.getLogger(
    InteractionController.class
  );

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  public String getInteraction(
    @PathVariable("id") Long interactionId,
    @PageableDefault(value = 10, page = 0) Pageable pageable,
    Model model
  ) {
    Long personId = AuthenticatedPerson.getPersonId();
    Interaction interaction = interactionService.getInteraction(interactionId);

    if (interaction instanceof Comment) {
      Comment comment = (Comment) interaction;

      logger.debug("Fetched interaction {} is a comment", interactionId);

      if (
        !(
          Hibernate.unproxy(
            comment.getReceiverInteraction()
          ) instanceof PeopleGroup
        )
      ) {
        logger.debug(
          "{} is not a group. Redirecting to parent interaction.",
          comment.getReceiverInteraction().getId()
        );
        return (
          "redirect:/interaction/" +
          comment.getReceiverInteraction().getId() +
          "?comment=" +
          comment.getId()
        );
      }
    }
    AccessLevel accessLevel = accessLevelService.getAccessLevelThatPersonHasOnInteraction(
      personId,
      interactionId
    );

    logger.debug(
      "accessLevel of interaction {} is {}",
      interactionId,
      accessLevel
    );

    model.addAttribute("accessLevel", accessLevel);
    model.addAttribute(
      "interactors",
      interactionService.getAppropriateInteractorListForPerson(
        personId,
        interactionId
      )
    );
    model.addAttribute("interaction", interaction);
    model.addAttribute(
      "comments",
      commentService.getCommentsFromInteraction(interactionId, pageable)
    );
    model.addAttribute("newComment", new NewComment(personId));
    if (interaction instanceof PeopleGroup) {
      logger.debug("{} is a group. Fetching additional info", interactionId);
      model.addAttribute("groupPfp", "/pfp/group/" + interactionId);
      model.addAttribute(
        "joinRequest",
        joinRequestService.getJoinRequestForPersonAndGroup(
          AuthenticatedPerson.getPersonId(),
          interactionId
        )
      );
    }

    return "interaction/display";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}/comment")
  public String commentOnInteraction(
    @PathVariable("id") Long interactionId,
    @ModelAttribute("newComment") @Valid NewComment newComment
  )
    throws NotAuthorizedException {
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
