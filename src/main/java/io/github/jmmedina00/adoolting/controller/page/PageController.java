package io.github.jmmedina00.adoolting.controller.page;

import io.github.jmmedina00.adoolting.controller.common.AuthenticatedPerson;
import io.github.jmmedina00.adoolting.dto.interaction.NewPost;
import io.github.jmmedina00.adoolting.entity.interaction.Post;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.service.InteractionService;
import io.github.jmmedina00.adoolting.service.InteractorService;
import io.github.jmmedina00.adoolting.service.interaction.PostService;
import io.github.jmmedina00.adoolting.service.page.PageLikeService;
import io.github.jmmedina00.adoolting.service.page.PageService;
import javax.validation.Valid;
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
@RequestMapping("/page/{id}")
public class PageController {
  @Autowired
  private PageService pageService;

  @Autowired
  private PageLikeService likeService;

  @Autowired
  private InteractionService interactionService;

  @Autowired
  private InteractorService interactorService;

  @Autowired
  private PostService postService;

  @RequestMapping(method = RequestMethod.GET)
  public String getPageProfile(
    @PathVariable("id") Long pageId,
    @PageableDefault(value = 10, page = 0) Pageable pageable,
    Model model
  ) {
    Page page = pageService.getPage(pageId);
    Person person = AuthenticatedPerson.getPerson();
    Long personId = person.getId();

    model.addAttribute("page", page);
    model.addAttribute("likeCount", likeService.getPageLikes(pageId));
    model.addAttribute(
      "givenLike",
      likeService.getLikeToPageFromPerson(person.getId(), pageId)
    );
    model.addAttribute(
      "interactors",
      interactorService.getRepresentableInteractorsByPerson(personId, pageId)
    );
    model.addAttribute(
      "posts",
      interactionService.getInteractions(pageId, pageable)
    );
    if (!model.containsAttribute("newPost")) {
      model.addAttribute("newPost", new NewPost(personId));
    }

    return "page/existing";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String createPostOnPage(
    @PathVariable("id") Long pageId,
    @ModelAttribute("newPost") @Valid NewPost newPost
  )
    throws NotAuthorizedException {
    Post post = postService.postOnProfile(
      AuthenticatedPerson.getPersonId(),
      pageId,
      newPost
    );
    return "redirect:/page/" + pageId + "?post=" + post.getId();
  }

  @RequestMapping(method = RequestMethod.POST, value = "/like")
  public String toggleLikeOnPage(@PathVariable("id") Long pageId) {
    likeService.toggleLikeToPage(AuthenticatedPerson.getPersonId(), pageId);
    return "redirect:/page/" + pageId;
  }
}
