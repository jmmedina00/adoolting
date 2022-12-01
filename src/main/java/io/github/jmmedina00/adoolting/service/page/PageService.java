package io.github.jmmedina00.adoolting.service.page;

import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.page.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageService {
  @Autowired
  private PageRepository pageRepository;

  public Page getPage(Long pageId) {
    return pageRepository.findById(pageId).orElse(null);
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
