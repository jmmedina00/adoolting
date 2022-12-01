package io.github.jmmedina00.adoolting.service.page;

import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.page.PageManager;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.page.PageManagerRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageManagerService {
  @Autowired
  private PageManagerRepository pageManagerRepository;

  List<Page> getPagesManagedByPerson(Long personId) {
    return pageManagerRepository
      .findPersonManagements(personId)
      .stream()
      .map(manager -> manager.getPage())
      .toList();
  }

  List<Person> getPeopleManagingPage(Long pageId) {
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

    return pageManagerRepository.save(manager);
  }
}
