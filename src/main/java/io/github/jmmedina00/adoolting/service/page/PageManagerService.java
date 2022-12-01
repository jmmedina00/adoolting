package io.github.jmmedina00.adoolting.service.page;

import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.page.PageManager;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.AlreadyInPlaceException;
import io.github.jmmedina00.adoolting.repository.page.PageManagerRepository;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageManagerService {
  @Autowired
  private PageManagerRepository pageManagerRepository;

  @Autowired
  private PersonService personService;

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

  public PageManager addManagerForPage(Long personId, Page page)
    throws Exception {
    Person person = personService.getPerson(personId);
    if (person == null) {
      throw new Exception();
    }

    List<Page> samePages = getPagesManagedByPerson(personId)
      .stream()
      .filter(managedPage -> managedPage.getId() == page.getId())
      .toList();
    if (samePages.size() > 0) {
      throw new AlreadyInPlaceException();
    }

    PageManager manager = new PageManager();

    manager.setPage(page);
    manager.setPerson(person);

    return pageManagerRepository.save(manager);
  }
}
