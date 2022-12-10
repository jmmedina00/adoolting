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

  public Page getPage(Long pageId) {
    return pageRepository.findActivePage(pageId).orElseThrow();
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
    Page page = pageRepository.findById(pageId).get();
    List<Person> managers = pageManagerService.getPeopleManagingPage(pageId);

    ArrayList<Person> finalized = new ArrayList<>(managers);
    finalized.add(page.getCreatedByPerson());
    return finalized;
  }

  public Page createPage(NewPage newPage, Long personId) {
    Person person = personService.getPerson(personId);
    Page page = new Page();
    page.setName(newPage.getName());
    page.setAbout(newPage.getAbout());
    page.setUrl(newPage.getUrl());
    page.setCreatedByPerson(person);

    return pageRepository.save(page);
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

    Person person = personService.getPerson(addedPersonId);
    return pageManagerService.addManagerForPage(person, page);
  }
}
