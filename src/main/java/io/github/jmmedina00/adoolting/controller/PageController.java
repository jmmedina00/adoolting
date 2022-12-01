package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.service.page.PageService;
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

    model.addAttribute("page", page);
    return "page/existing";
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
