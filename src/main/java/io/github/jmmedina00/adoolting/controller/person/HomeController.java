package io.github.jmmedina00.adoolting.controller.person;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.service.ConfirmableInteractionService;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/home")
public class HomeController {
  @Autowired
  private InteractionService interactionService;

  @Autowired
  private ConfirmableInteractionService cInteractionService;

  @Autowired
  private PageService pageService;

  @RequestMapping(method = RequestMethod.GET)
  public String getHomePage(
    @PageableDefault(value = 10, page = 0) Pageable pageable,
    Model model
  ) {
    Person person = AuthenticatedPerson.getPerson();
    ArrayList<Interactor> interestingInteractors = new ArrayList<>();
    interestingInteractors.addAll(
      cInteractionService.getPersonFriends(person.getId())
    );
    interestingInteractors.addAll(
      pageService.getPagesLikedByPerson(person.getId())
    );
    interestingInteractors.add(person);

    List<Long> interactorIds = interestingInteractors
      .stream()
      .map(interactor -> interactor.getId())
      .toList();
    Page<Interaction> interactions = interactionService.getInteractionsFromInteractors(
      interactorIds,
      pageable
    );

    model.addAttribute("interactions", interactions);
    model.addAttribute(
      "commonfriends",
      cInteractionService.getPersonFriendsOfFriends(person.getId())
    );

    return "person/home";
  }
}
