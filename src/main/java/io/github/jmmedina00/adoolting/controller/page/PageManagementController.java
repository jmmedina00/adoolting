package io.github.jmmedina00.adoolting.controller.page;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.interaction.ProfilePictureFile;
import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.exception.AlreadyInPlaceException;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.page.PageManagerService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/page/{id}/manage")
public class PageManagementController {
  @Autowired
  private PageService pageService;

  @Autowired
  private PageManagerService managerService;

  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @RequestMapping(method = RequestMethod.GET)
  public String getPageManagementPage(
    @PathVariable("id") Long pageId,
    Model model
  )
    throws NotAuthorizedException {
    Long personId = AuthenticatedPerson.getPersonId();
    Page page = pageService.getPage(pageId);

    if (!Objects.equals(page.getCreatedByPerson().getId(), personId)) {
      throw new NotAuthorizedException();
    }

    model.addAttribute("page", page);
    model.addAttribute("form", pageService.getPageForm(pageId));
    model.addAttribute("pfp", new ProfilePictureFile());
    model.addAttribute("managers", pageService.getPageManagers(pageId));
    model.addAttribute(
      "friends",
      cInteractionService.getPersonFriends(personId)
    );
    return "page/manage";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/delete")
  public String showDeleteForm(@PathVariable("id") Long pageId, Model model)
    throws NotAuthorizedException {
    Long personId = AuthenticatedPerson.getPersonId();
    Page page = pageService.getPage(pageId);

    if (!Objects.equals(page.getCreatedByPerson().getId(), personId)) {
      throw new NotAuthorizedException();
    }

    model.addAttribute("page", page);
    model.addAttribute("confirm", new SecureDeletion());
    return "page/delete";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String updatePage(
    @PathVariable("id") Long pageId,
    @ModelAttribute("form") @Valid NewPage form
  )
    throws NotAuthorizedException {
    pageService.updatePage(pageId, AuthenticatedPerson.getPersonId(), form);

    return "redirect:/page/" + pageId + "/manage";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{personId}")
  public String addPersonAsManager(
    @PathVariable("id") Long pageId,
    @PathVariable("personId") Long personId
  )
    throws Exception {
    pageService.addManagerToPage(
      AuthenticatedPerson.getPersonId(),
      personId,
      pageId
    );
    return "redirect:/page/" + pageId + "/manage";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/{personId}/remove")
  public String removePersonFromManagingPage(
    @PathVariable("id") Long pageId,
    @PathVariable("personId") Long personId
  )
    throws NotAuthorizedException {
    Long authPersonId = AuthenticatedPerson.getPersonId();
    managerService.removeManagerFromPage(pageId, personId, authPersonId);

    return Objects.equals(authPersonId, personId)
      ? "redirect:/profile"
      : "redirect:/page/" + pageId + "/manage";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/delete")
  public String deletePage(
    @PathVariable("id") Long pageId,
    @ModelAttribute("confirm") @Valid SecureDeletion confirmation
  )
    throws Exception {
    pageService.deletePage(
      pageId,
      AuthenticatedPerson.getPersonId(),
      confirmation
    );
    return "redirect:/profile";
  }

  @ExceptionHandler(AlreadyInPlaceException.class)
  public String redirectToManagementWithError(AlreadyInPlaceException e) {
    return "redirect:/page/" + e.getPageId() + "/manage?error";
  }
}
