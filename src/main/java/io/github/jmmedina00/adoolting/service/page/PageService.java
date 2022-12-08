package io.github.jmmedina00.adoolting.service.page;

import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.page.PageRepository;
import java.util.ArrayList;
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

  public Page getPage(Long pageId) {
    return pageRepository.findById(pageId).orElse(null);
  }

  public boolean isPageManagedByPerson(Long pageId, Long personId) {
    Page page = getPage(pageId);

    if (page == null) {
      return false;
    }

    Optional<Person> found = getPageManagers(pageId)
      .stream()
      .filter(foundPerson -> Objects.equals(foundPerson.getId(), personId))
      .findFirst();
    return found.isPresent();
  }

  public List<Page> getAllPersonPages(Person person) {
    Long personId = person.getId();
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

  public Page createPage(NewPage newPage, Person person) {
    Page page = new Page();
    page.setName(newPage.getName());
    page.setAbout(newPage.getAbout());
    page.setUrl(newPage.getUrl());
    page.setCreatedByPerson(person);

    return pageRepository.save(page);
  }
}
