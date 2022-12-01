package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.interaction.NewPostOnPage;
import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.exception.AlreadyInPlaceException;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.interaction.PostService;
import io.github.jmmedina00.adoolting.service.page.PageManagerService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/page")
public class PageController {
  @Autowired
  private PageService pageService;

  @Autowired
  private PageManagerService managerService;

  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private PostService postService;

  @RequestMapping(method = RequestMethod.GET)
  public String getNewPageForm(Model model) {
    if (!model.containsAttribute("page")) {
      model.addAttribute("page", new NewPage());
    }

    return "page/new";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{id}")
  public String getPageProfile(
    @PathVariable("id") String pageIdStr,
    Model model
  ) {
    Long pageId;
    try {
      pageId = Long.parseLong(pageIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    Page page = pageService.getPage(pageId);
    if (page == null) {
      return "redirect:/home?notfound";
    }

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    List<Interactor> controlledInteractors;
    if (pageService.isPageManagedByPerson(page, authenticatedPerson)) {
      controlledInteractors = List.of(authenticatedPerson, page);
    } else {
      controlledInteractors =
        new ArrayList<>(pageService.getAllPersonPages(authenticatedPerson));
      controlledInteractors.add(0, authenticatedPerson);
    }

    model.addAttribute("page", page);
    model.addAttribute("interactors", controlledInteractors);
    model.addAttribute("posts", interactionService.getInteractions(pageId));
    model.addAttribute("newPost", new NewPostOnPage());
    return "page/existing";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}")
  public String createPostOnPage(
    @PathVariable("id") String pageIdStr,
    @ModelAttribute("newPost") @Valid NewPostOnPage newPost,
    BindingResult result
  ) {
    Long pageId;
    try {
      pageId = Long.parseLong(pageIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    if (result.hasErrors()) {
      return "redirect:/page/" + pageId + "?error";
    }

    return "redirect:/page/" + pageId + "?success";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/{id}/manage")
  public String getPageManagementPage(
    @PathVariable("id") String pageIdStr,
    Model model
  ) {
    Long pageId;
    try {
      pageId = Long.parseLong(pageIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    Page page = pageService.getPage(pageId);
    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    if (
      page == null ||
      !Objects.equals(
        page.getCreatedByPerson().getId(),
        authenticatedPerson.getId()
      )
    ) {
      return "redirect:/home?notfound";
    }

    model.addAttribute("page", page);
    model.addAttribute("managers", pageService.getPageManagers(pageId));
    model.addAttribute(
      "friends",
      cInteractionService.getPersonFriends(authenticatedPerson)
    );
    return "page/manage";
  }

  @RequestMapping(
    method = RequestMethod.POST,
    value = "/{pageId}/manage/{personId}"
  )
  public String addPersonAsManager(
    @PathVariable("pageId") String pageIdStr,
    @PathVariable("personId") String personIdStr
  ) {
    Long pageId;
    Long personId;
    try {
      pageId = Long.parseLong(pageIdStr);
      personId = Long.parseLong(personIdStr);
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }

    Page page = pageService.getPage(pageId);
    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    if (
      page == null ||
      !Objects.equals(
        page.getCreatedByPerson().getId(),
        authenticatedPerson.getId()
      )
    ) {
      return "redirect:/home?notfound";
    }

    try {
      managerService.addManagerForPage(personId, page);
      return "redirect:/page/" + pageId + "/manage";
    } catch (AlreadyInPlaceException e) {
      return "redirect:/page/" + pageId + "/manage?error";
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }
  }

  @RequestMapping(method = RequestMethod.POST)
  public String createNewPage(
    @ModelAttribute("page") @Valid NewPage newPage,
    BindingResult result,
    RedirectAttributes attributes
  ) {
    if (result.hasErrors()) {
      attributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.page",
        result
      );
      attributes.addFlashAttribute("page", newPage);
      return "redirect:/page";
    }

    Person authenticatedPerson =
      (
        (PersonDetails) SecurityContextHolder
          .getContext()
          .getAuthentication()
          .getPrincipal()
      ).getPerson();

    Page page = pageService.createPage(newPage, authenticatedPerson);

    return "redirect:/page/" + page.getId();
  }
}
