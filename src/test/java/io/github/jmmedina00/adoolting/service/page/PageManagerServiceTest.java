package io.github.jmmedina00.adoolting.service.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.page.PageManager;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.page.PageManagerRepository;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PageManagerServiceTest {
  @MockBean
  private PageManagerRepository pageManagerRepository;

  @Autowired
  private PageManagerService pageManagerService;

  @Test
  public void getPagesManagedByPersonExtractsPagesFromRecords() {
    Person person = new Person();
    Page pageFoo = new Page();
    PageManager foo = new PageManager();
    foo.setPerson(person);
    foo.setPage(pageFoo);

    Page pageBar = new Page();
    PageManager bar = new PageManager();
    bar.setPerson(person);
    bar.setPage(pageBar);

    Mockito
      .when(pageManagerRepository.findPersonManagements(anyLong()))
      .thenReturn(List.of(foo, bar));
    assertEquals(
      List.of(pageFoo, pageBar),
      pageManagerService.getPagesManagedByPerson(2L)
    );
  }

  @Test
  public void getPeopleManagingPageExtractPersonsFromRecords() {
    Page page = new Page();
    Person personFoo = new Person();
    PageManager foo = new PageManager();
    foo.setPage(page);
    foo.setPerson(personFoo);

    Person personBar = new Person();
    PageManager bar = new PageManager();
    bar.setPage(page);
    bar.setPerson(personBar);

    Mockito
      .when(pageManagerRepository.findPageManagers(anyLong()))
      .thenReturn(List.of(foo, bar));
    assertEquals(
      List.of(personFoo, personBar),
      pageManagerService.getPeopleManagingPage(5L)
    );
  }

  @Test
  public void addManagerForPageCreatesManagerWithSpecifiedEntities() {
    Person person = new Person();
    Page page = new Page();

    Mockito
      .when(pageManagerRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PageManager manager = pageManagerService.addManagerForPage(person, page);
    assertEquals(person, manager.getPerson());
    assertEquals(page, manager.getPage());
    verify(pageManagerRepository, times(1)).save(manager);
  }
}
