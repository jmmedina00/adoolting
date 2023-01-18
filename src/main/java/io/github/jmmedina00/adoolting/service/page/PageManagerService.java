package io.github.jmmedina00.adoolting.service.page;

import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.page.PageManager;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.page.PageManagerRepository;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageManagerService {
  @Autowired
  private PageManagerRepository pageManagerRepository;

  private static final Logger logger = LoggerFactory.getLogger(
    PageManagerService.class
  );

  public List<Page> getPagesManagedByPerson(Long personId) {
    return pageManagerRepository
      .findPersonManagements(personId)
      .stream()
      .map(manager -> manager.getPage())
      .toList();
  }

  public List<Person> getPeopleManagingPage(Long pageId) {
    return pageManagerRepository
      .findPageManagers(pageId)
      .stream()
      .map(manager -> manager.getPerson())
      .toList();
  }

  public PageManager addManagerForPage(Person person, Page page) {
    PageManager manager = new PageManager();

    manager.setPage(page);
    manager.setPerson(person);

    PageManager saved = pageManagerRepository.save(manager);
    logger.info(
      "Person {} has been added as manager of page {}; id={}",
      person.getId(),
      page.getId(),
      saved.getId()
    );
    return saved;
  }

  public PageManager removeManagerFromPage(
    Long pageId,
    Long personId,
    Long attemptingPersonId
  )
    throws NotAuthorizedException {
    PageManager manager = pageManagerRepository
      .findPageManagerInstance(pageId, personId)
      .orElseThrow(NotAuthorizedException::new);

    Long actualPageCreatorId = manager.getPage().getCreatedByPerson().getId();

    if (
      List.of(personId, actualPageCreatorId).indexOf(attemptingPersonId) == -1
    ) {
      throw new NotAuthorizedException();
    }

    manager.setDeletedAt(new Date());
    PageManager saved = pageManagerRepository.save(manager);

    if (Objects.equals(attemptingPersonId, personId)) {
      logger.info(
        "Person {} has removed themselves from managing page {}",
        personId,
        pageId
      );
    } else {
      logger.info(
        "Person {} has revoked person {} from managing page {}",
        attemptingPersonId,
        personId,
        pageId
      );
    }

    return saved;
  }
}
