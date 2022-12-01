package io.github.jmmedina00.adoolting.service.page;

import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.page.PageRepository;
import java.util.ArrayList;
import java.util.List;
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
