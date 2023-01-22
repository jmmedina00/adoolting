package io.github.jmmedina00.adoolting.controller;

import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/search")
public class SearchController {
  @Autowired
  private PersonService personService;

  @Autowired
  private PageService pageService;

  @RequestMapping(method = RequestMethod.GET)
  public String getSearchResults(
    @RequestParam("terms") String terms,
    Model model
  ) {
    PageRequest request = PageRequest.of(0, 6);

    model.addAttribute(
      "persons",
      personService.getPersonsBySearchTerm(terms, request)
    );
    model.addAttribute(
      "pages",
      pageService.getPagesBySearchTerm(terms, request)
    );
    return "search/general";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/person")
  public String getPersonSearchResults(
    @RequestParam("terms") String terms,
    @PageableDefault(value = 10, page = 0) Pageable pageable,
    Model model
  ) {
    model.addAttribute(
      "results",
      personService.getPersonsBySearchTerm(terms, pageable)
    );
    return "search/more";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/page")
  public String getPageSearchResults(
    @RequestParam("terms") String terms,
    @PageableDefault(value = 10, page = 0) Pageable pageable,
    Model model
  ) {
    model.addAttribute(
      "results",
      pageService.getPagesBySearchTerm(terms, pageable)
    );
    return "search/more";
  }
}
