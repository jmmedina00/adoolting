package io.github.jmmedina00.adoolting.service.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.page.NewPage;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.AlreadyInPlaceException;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.page.PageRepository;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.validation.BindException;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PageServiceTest {
  @MockBean
  private PageRepository pageRepository;

  @MockBean
  private PageManagerService pageManagerService;

  @MockBean
  private PersonService personService;

  @Autowired
  private PageService pageService;

  @Test
  public void getPageGetsPageStraightFromRepository() {
    Page page = new Page();
    Mockito
      .when(pageRepository.findActivePage(anyLong()))
      .thenReturn(Optional.of(page));

    Page returned = pageService.getPage(10L);
    assertEquals(page, returned);
  }

  @Test
  public void getAllPersonPagesReturnsPagesCreatedByAndManagedByPerson() {
    Page a = new Page();
    a.setId(1L);
    Page b = new Page();
    b.setId(2L);
    Page c = new Page();
    c.setId(3L);
    Page d = new Page();
    d.setId(4L);
    Page e = new Page();
    e.setId(5L);
    Page f = new Page();
    f.setId(6L);

    Mockito
      .when(pageRepository.findPagesCreatedByPerson(anyLong()))
      .thenReturn(List.of(a, d, e));
    Mockito
      .when(pageManagerService.getPagesManagedByPerson(anyLong()))
      .thenReturn(List.of(b, c, f));

    assertEquals(List.of(a, b, c, d, e, f), pageService.getAllPersonPages(1L));
  }

  @Test
  public void getPageManagersReturnsPageManagersPlusPageCreator() {
    Page page = new Page();
    Person creator = new Person();
    page.setCreatedByPerson(creator);

    Person foo = new Person();
    Person bar = new Person();
    Person baz = new Person();

    Mockito
      .when(pageRepository.findActivePage(anyLong()))
      .thenReturn(Optional.of(page));
    Mockito
      .when(pageManagerService.getPeopleManagingPage(anyLong()))
      .thenReturn(List.of(foo, bar, baz));

    List<Person> result = pageService.getPageManagers(1L);
    assertEquals(List.of(foo, bar, baz, creator), result);
  }

  @ParameterizedTest
  @CsvSource(
    {
      "12,false",
      "125,true",
      "200,false",
      "409,false",
      "25,true",
      "98,true",
      "259,false",
      "250,true",
    }
  )
  public void isPageManagedByPersonReturnsResultAccordingToPersonIds(
    Long personId,
    boolean expected
  ) {
    Page page = new Page();
    Person creator = new Person();
    creator.setId(125L);
    page.setCreatedByPerson(creator);

    Person foo = new Person();
    foo.setId(25L);
    Person bar = new Person();
    bar.setId(98L);
    Person baz = new Person();
    baz.setId(250L);

    Mockito
      .when(pageRepository.findActivePage(anyLong()))
      .thenReturn(Optional.of(page));
    Mockito
      .when(pageManagerService.getPeopleManagingPage(anyLong()))
      .thenReturn(List.of(foo, bar, baz));

    assertEquals(expected, pageService.isPageManagedByPerson(1L, personId));
  }

  @Test
  public void getPageFormTransfersPageInfoBackIntoForm() {
    Page page = new Page();
    page.setName("Page");
    page.setAbout("This is a page");
    page.setUrl("http://test.local");

    Mockito
      .when(pageRepository.findActivePage(4L))
      .thenReturn(Optional.of(page));

    NewPage form = pageService.getPageForm(4L);

    assertEquals(page.getName(), form.getName());
    assertEquals(page.getAbout(), form.getAbout());
    assertEquals(page.getUrl(), form.getUrl());
  }

  @Test
  public void updatePageUpdatesPageInformationAndSavesToRepository()
    throws NotAuthorizedException {
    Person person = new Person();
    person.setId(3L);

    Page page = new Page();
    page.setName("Test");
    page.setAbout("Testing");
    page.setUrl("www.test.local");
    page.setCreatedByPerson(person);

    NewPage newPage = new NewPage();
    newPage.setName("Page");
    newPage.setAbout("This is a page");
    newPage.setUrl("http://test.local");

    Mockito
      .when(pageRepository.findActivePage(4L))
      .thenReturn(Optional.of(page));
    Mockito
      .when(pageRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    Page saved = pageService.updatePage(4L, 3L, newPage);
    assertEquals(page, saved);
    assertEquals(newPage.getName(), saved.getName());
    assertEquals(newPage.getAbout(), saved.getAbout());
    assertEquals(newPage.getUrl(), saved.getUrl());
  }

  @Test
  public void updatePageThrowsIfSomeoneOtherThanPageCreatorTriesToUpdateThePage() {
    Person person = new Person();
    person.setId(3L);

    Page page = new Page();
    page.setName("Test");
    page.setAbout("Testing");
    page.setUrl("www.test.local");
    page.setCreatedByPerson(person);

    NewPage newPage = new NewPage();
    newPage.setName("Page");
    newPage.setAbout("This is a page");
    newPage.setUrl("http://test.local");

    Mockito
      .when(pageRepository.findActivePage(4L))
      .thenReturn(Optional.of(page));

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        pageService.updatePage(4L, 5L, newPage);
      }
    );
  }

  @Test
  public void createPageCreatesPageWithSpecifiedDetails() {
    Person creator = new Person();

    Mockito.when(personService.getPerson(anyLong())).thenReturn(creator);
    Mockito
      .when(pageRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    NewPage newPage = new NewPage();
    newPage.setName("Page");
    newPage.setAbout("This is a page");
    newPage.setUrl("http://test.local");

    Page page = pageService.createPage(newPage, 2L);

    assertEquals(newPage.getName(), page.getName());
    assertEquals(newPage.getAbout(), page.getAbout());
    assertEquals(newPage.getUrl(), page.getUrl());
    assertEquals(creator, page.getCreatedByPerson());
  }

  @Test
  public void deletePageSetsPageToDeletedOnlyIfCorrectPersonCallsIt()
    throws Exception {
    Long id = 23L;
    Person creator = new Person();
    creator.setId(id);
    Page page = new Page();
    page.setCreatedByPerson(creator);

    Person returned = new Person();
    returned.setId(id);

    Mockito
      .when(pageRepository.findActivePage(2L))
      .thenReturn(Optional.of(page));
    Mockito
      .when(pageRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    Mockito
      .when(personService.getPersonWithMatchingPassword(eq(id), any()))
      .thenReturn(returned);

    Page result = pageService.deletePage(2L, id, new SecureDeletion());
    assertEquals(page, result);
    assertNotNull(result.getDeletedAt());
  }

  @Test
  public void deletePageThrowsExceptionGeneratedByPersonService()
    throws Exception {
    Long id = 23L;
    Person creator = new Person();
    creator.setId(id);
    Page page = new Page();
    page.setCreatedByPerson(creator);

    Mockito
      .when(pageRepository.findActivePage(2L))
      .thenReturn(Optional.of(page));
    Mockito
      .when(personService.getPersonWithMatchingPassword(anyLong(), any()))
      .thenThrow(new BindException(new NewPage(), "test"));

    assertThrows(
      BindException.class,
      () -> {
        pageService.deletePage(2L, id, new SecureDeletion());
      }
    );

    verify(pageRepository, never()).save(page);
  }

  @Test
  public void deletePageIsOnlyAllowedToBeRunByPageCreator() throws Exception {
    Person creator = new Person();
    creator.setId(23L);
    Page page = new Page();
    page.setCreatedByPerson(creator);

    Person returned = new Person();
    returned.setId(24L);

    Mockito
      .when(pageRepository.findActivePage(2L))
      .thenReturn(Optional.of(page));
    Mockito
      .when(personService.getPersonWithMatchingPassword(eq(24L), any()))
      .thenReturn(returned);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        pageService.deletePage(2L, 24L, new SecureDeletion());
      }
    );

    verify(pageRepository, never()).save(page);
  }

  @Test
  public void addManagerToPageCallsPageManagerService() throws Exception {
    Person creator = new Person();
    creator.setId(23L);
    Page page = new Page();
    page.setCreatedByPerson(creator);

    Person person = new Person();

    Mockito
      .when(pageRepository.findActivePage(2L))
      .thenReturn(Optional.of(page));
    Mockito
      .when(pageManagerService.getPeopleManagingPage(2L))
      .thenReturn(List.of());
    Mockito.when(personService.getPerson(25L)).thenReturn(person);

    pageService.addManagerToPage(23L, 25L, 2L);
    verify(pageManagerService, times(1)).addManagerForPage(person, page);
  }

  @Test
  public void addManagerToPageOnlyAllowedToBeRunByCreator() throws Exception {
    Person creator = new Person();
    creator.setId(23L);
    Page page = new Page();
    page.setCreatedByPerson(creator);

    Person manager = new Person();
    manager.setId(30L);

    Mockito
      .when(pageRepository.findActivePage(2L))
      .thenReturn(Optional.of(page));
    Mockito
      .when(pageManagerService.getPeopleManagingPage(2L))
      .thenReturn(List.of(manager));

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        pageService.addManagerToPage(30L, 25L, 2L); // Throws when run by manager
      }
    );

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        pageService.addManagerToPage(47L, 25L, 2L); // Throws when run by anyone else
      }
    );
  }

  @Test
  public void addManagerToPageThrowsWhenTryingToAddActiveManagerAgain()
    throws Exception {
    Person creator = new Person();
    creator.setId(23L);
    Page page = new Page();
    page.setCreatedByPerson(creator);

    Person manager = new Person();
    manager.setId(30L);

    Mockito
      .when(pageRepository.findActivePage(2L))
      .thenReturn(Optional.of(page));
    Mockito
      .when(pageManagerService.getPeopleManagingPage(2L))
      .thenReturn(List.of(manager));

    assertThrows(
      AlreadyInPlaceException.class,
      () -> {
        pageService.addManagerToPage(23L, 30L, 2L);
      }
    );
  }
}
