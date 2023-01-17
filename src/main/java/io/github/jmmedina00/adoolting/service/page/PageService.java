package io.github.jmmedina00.adoolting.service.page;

import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.page.PageManager;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.AlreadyInPlaceException;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.page.PageRepository;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageService {
  @Autowired
  private PageRepository pageRepository;

  @Autowired
  private PageManagerService pageManagerService;

  @Autowired
  private PersonService personService;

  private static final Logger logger = LoggerFactory.getLogger(
    PageService.class
  );

  public Page getPage(Long pageId) {
    return pageRepository.findActivePage(pageId).orElseThrow();
  }

  public List<Page> getPagesLikedByPerson(Long personId) {
    return pageRepository.findPagesLikedByPerson(personId);
  }

  public boolean isPageManagedByPerson(Long pageId, Long personId) {
    Optional<Person> found = getPageManagers(pageId)
      .stream()
      .filter(foundPerson -> Objects.equals(foundPerson.getId(), personId))
      .findFirst();
    return found.isPresent();
  }

  public List<Page> getAllPersonPages(Long personId) {
    List<Page> createdByPerson = pageRepository.findPagesCreatedByPerson(
      personId
    );
    List<Page> addedAsManager = pageManagerService.getPagesManagedByPerson(
      personId
    );

    ArrayList<Page> all = new ArrayList<>();
    all.addAll(createdByPerson);
    all.addAll(addedAsManager);
    all.sort((a, b) -> a.getId().compareTo(b.getId()));

    return all;
  }

  public List<Person> getPageManagers(Long pageId) {
    Page page = getPage(pageId);
    List<Person> managers = pageManagerService.getPeopleManagingPage(pageId);

    ArrayList<Person> finalized = new ArrayList<>(managers);
    finalized.add(page.getCreatedByPerson());
    return finalized;
  }

  public NewPage getPageForm(Long pageId) {
    Page page = getPage(pageId);
    NewPage form = new NewPage();
    form.setName(page.getName());
    form.setAbout(page.getAbout());
    form.setUrl(page.getUrl());

    return form;
  }

  public Page updatePage(Long pageId, Long personId, NewPage form)
    throws NotAuthorizedException {
    Page page = getPage(pageId);

    if (!Objects.equals(personId, page.getCreatedByPerson().getId())) {
      throw new NotAuthorizedException();
    }

    page.setName(form.getName());
    page.setAbout(form.getAbout());
    page.setUrl(form.getUrl());

    Page saved = pageRepository.save(page);
    logger.info("Page {} has been updated by person {}", pageId, personId);
    return saved;
  }

  public Page createPage(NewPage newPage, Long personId) {
    Person person = personService.getPerson(personId);
    Page page = new Page();
    page.setName(newPage.getName());
    page.setAbout(newPage.getAbout());
    page.setUrl(newPage.getUrl());
    page.setCreatedByPerson(person);

    Page saved = pageRepository.save(page);
    logger.info(
      "New page (id={}) created by person {}",
      saved.getId(),
      personId
    );
    return saved;
  }

  public Page deletePage(
    Long pageId,
    Long attemptingPersonId,
    SecureDeletion confirmation
  )
    throws Exception {
    Page page = getPage(pageId);
    Long creatorId = page.getCreatedByPerson().getId();
    Person person = personService.getPersonWithMatchingPassword(
      attemptingPersonId,
      confirmation
    );

    if (!Objects.equals(creatorId, person.getId())) {
      throw new NotAuthorizedException();
    }

    page.setDeletedAt(new Date());
    logger.info(
      "Page {} has been deleted by person {}",
      pageId,
      attemptingPersonId
    );
    return pageRepository.save(page);
  }

  public PageManager addManagerToPage(
    Long attemptingPersonId,
    Long addedPersonId,
    Long pageId
  )
    throws Exception {
    Page page = getPage(pageId);
    Long creatorId = page.getCreatedByPerson().getId();

    if (!Objects.equals(creatorId, attemptingPersonId)) {
      throw new NotAuthorizedException();
    }

    if (isPageManagedByPerson(pageId, addedPersonId)) {
      throw new AlreadyInPlaceException(pageId);
    }

    logger.debug(
      "Person {} is the creator of page {} and can thus add person {} as manager",
      attemptingPersonId,
      pageId,
      addedPersonId
    );

    Person person = personService.getPerson(addedPersonId);
    return pageManagerService.addManagerForPage(person, page);
  }
}
