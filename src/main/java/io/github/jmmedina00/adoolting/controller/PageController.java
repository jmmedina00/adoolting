package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.dto.page.NewPage;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/page")
public class PageController {

  @RequestMapping(method = RequestMethod.GET)
  public String getNewPageForm(Model model) {
    if (!model.containsAttribute("page")) {
      model.addAttribute("page", new NewPage());
    }

    return "page/new";
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

    return "redirect:/page?success";
  }
}
