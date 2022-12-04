package io.github.jmmedina00.adoolting.service.page;

import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.page.PageLike;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.page.PageLikeRepository;
import io.github.jmmedina00.adoolting.service.InteractorService;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageLikeService {
  @Autowired
  private PageLikeRepository likeRepository;

  @Autowired
  private InteractorService interactorService;

  public Long getPageLikes(Long pageId) {
    return likeRepository.countPageLikes(pageId);
  }

  public PageLike getLikeToPageFromPerson(Long personId, Long pageId) {
    return likeRepository.findPageLikeFromPerson(personId, pageId);
  }

  public PageLike toggleLikeToPage(Long personId, Long pageId) {
    PageLike existing = getLikeToPageFromPerson(personId, pageId);

    if (existing != null) {
      existing.setDeletedAt(new Date());
      return likeRepository.save(existing);
    }

    Person person = (Person) interactorService.getInteractor(personId);
    Page page = (Page) interactorService.getInteractor(pageId);
    PageLike like = new PageLike();
    like.setInteractor(person);
    like.setReceiverInteractor(page);

    return likeRepository.save(like);
  }
}
