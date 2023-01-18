package io.github.jmmedina00.adoolting.service.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.page.PageManager;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.page.PageManagerRepository;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import java.util.Optional;
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

  @Test
  public void removeManagerFromPageFetchesManagerEntityAndSetsItAsDeleted()
    throws NotAuthorizedException {
    Person creator = new Person();
    creator.setId(14L);

    Page page = new Page();
    page.setId(15L);
    page.setCreatedByPerson(creator);

    PageManager manager = new PageManager();
    manager.setPage(page);

    Mockito
      .when(pageManagerRepository.findPageManagerInstance(15L, 17L))
      .thenReturn(Optional.of(manager));
    Mockito
      .when(pageManagerRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PageManager deleted = pageManagerService.removeManagerFromPage(
      15L,
      17L,
      14L
    );

    assertEquals(manager, deleted);
    assertNotNull(deleted.getDeletedAt());
  }

  @Test
  public void removeManagerFromPageAllowsManagerToRemoveThemselvesFromPageAsWell()
    throws NotAuthorizedException {
    Person creator = new Person();
    creator.setId(14L);

    Page page = new Page();
    page.setId(15L);
    page.setCreatedByPerson(creator);

    PageManager manager = new PageManager();
    manager.setPage(page);

    Mockito
      .when(pageManagerRepository.findPageManagerInstance(15L, 17L))
      .thenReturn(Optional.of(manager));
    Mockito
      .when(pageManagerRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PageManager deleted = pageManagerService.removeManagerFromPage(
      15L,
      17L,
      17L
    );

    assertEquals(manager, deleted);
    assertNotNull(deleted.getDeletedAt());
  }

  @Test
  public void removeManagerFromPageThrowsIfNoManagerEntityCanBeFound() {
    Mockito
      .when(pageManagerRepository.findPageManagerInstance(15L, 17L))
      .thenReturn(Optional.empty());

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        pageManagerService.removeManagerFromPage(15L, 17L, 14L);
      }
    );

    verify(pageManagerRepository, never()).save(any());
  }

  @Test
  public void removeManagerFromPageThrowsIfManagerIsAttemptedToBeRemovedBySomeoneOtherThanPageCreator() {
    Person creator = new Person();
    creator.setId(14L);

    Page page = new Page();
    page.setId(15L);
    page.setCreatedByPerson(creator);

    PageManager manager = new PageManager();
    manager.setPage(page);

    Mockito
      .when(pageManagerRepository.findPageManagerInstance(15L, 17L))
      .thenReturn(Optional.of(manager));

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        pageManagerService.removeManagerFromPage(15L, 17L, 13L);
      }
    );
  }
}
