package io.github.jmmedina00.adoolting.controller.page;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.interaction.NewPostOnPage;
import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.AlreadyInPlaceException;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.interaction.PostService;
import io.github.jmmedina00.adoolting.service.page.PageLikeService;
import io.github.jmmedina00.adoolting.service.page.PageManagerService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
  private PageLikeService likeService;

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

    Person person = AuthenticatedPerson.getPerson();
    Long personId = person.getId();

    List<Interactor> controlledInteractors;
    if (pageService.isPageManagedByPerson(pageId, personId)) {
      controlledInteractors = List.of(person, page);
    } else {
      controlledInteractors =
        new ArrayList<>(pageService.getAllPersonPages(personId));
      controlledInteractors.add(0, person);
    }

    model.addAttribute("page", page);
    model.addAttribute("likeCount", likeService.getPageLikes(pageId));
    model.addAttribute(
      "givenLike",
      likeService.getLikeToPageFromPerson(person.getId(), pageId)
    );
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

    try {
      Post post = postService.postOnPage(newPost, pageId);
      return "redirect:/page/" + pageId + "?post=" + post.getId();
    } catch (Exception e) {
      return "redirect:/page/" + pageId + "?error";
    }
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

    Long personId = AuthenticatedPerson.getPersonId();
    Page page = pageService.getPage(pageId);

    if (
      page == null ||
      !Objects.equals(page.getCreatedByPerson().getId(), personId)
    ) {
      return "redirect:/home?notfound";
    }

    model.addAttribute("page", page);
    model.addAttribute("pfp", new ProfilePictureFile());
    model.addAttribute("managers", pageService.getPageManagers(pageId));
    model.addAttribute(
      "friends",
      cInteractionService.getPersonFriends(personId)
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

    // TODO: security checks to service
    if (
      page == null ||
      !Objects.equals(
        page.getCreatedByPerson().getId(),
        AuthenticatedPerson.getPersonId()
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

    Page page = pageService.createPage(
      newPage,
      AuthenticatedPerson.getPersonId()
    );
    return "redirect:/page/" + page.getId();
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{id}/like")
  public String toggleLikeOnPage(@PathVariable("id") String pageIdStr) {
    try {
      Long pageId = Long.parseLong(pageIdStr);
      likeService.toggleLikeToPage(AuthenticatedPerson.getPersonId(), pageId);
      return "redirect:/page/" + pageId;
    } catch (Exception e) {
      return "redirect:/home?notfound";
    }
  }
}
