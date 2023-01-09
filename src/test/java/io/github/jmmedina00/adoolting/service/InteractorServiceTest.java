package io.github.jmmedina00.adoolting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.exception.NotAuthorizedException;
import io.github.jmmedina00.adoolting.repository.InteractorRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import java.util.NoSuchElementException;
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
public class InteractorServiceTest {
  @MockBean
  private InteractorRepository interactorRepository;

  @MockBean
  private PageService pageService;

  @Autowired
  private InteractorService interactorService;

  @Test
  public void getInteractorGetsInteractorStraightFromRepository() {
    Person person = new Person();

    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.of(person));

    Interactor interactor = interactorService.getInteractor(4L);
    assertEquals(person, interactor);
  }

  @Test
  public void getInteractorThrowsWhenInteractorCannotBeFound() {
    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.empty());
    assertThrows(
      NoSuchElementException.class,
      () -> {
        interactorService.getInteractor(4L);
      }
    );
  }

  @Test
  public void getRepresentableInteractorByPersonGetsPersonIfIdsMatch()
    throws NotAuthorizedException {
    Person person = new Person();
    person.setId(3L);

    Mockito
      .when(interactorRepository.findById(3L))
      .thenReturn(Optional.of(person));

    Interactor interactor = interactorService.getRepresentableInteractorByPerson(
      3L,
      3L
    );
    assertEquals(person, interactor);
  }

  @Test
  public void getRepresentableInteractorByPersonThrowsIfPersonAttemptedToRepresentDoesNotMatchPersonAttempting() {
    Person foo = new Person();
    foo.setId(3L);
    Person bar = new Person();
    bar.setId(4L);

    Mockito
      .when(interactorRepository.findById(3L))
      .thenReturn(Optional.of(foo));
    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.of(bar));

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        interactorService.getRepresentableInteractorByPerson(4L, 3L);
      }
    );
  }

  @Test
  public void getRepresentableInteractorByPersonChecksInPageServiceBeforeLettingPersonRepresentIt()
    throws NotAuthorizedException {
    Person person = new Person();
    person.setId(3L);
    Page page = new Page();
    page.setId(4L);

    Mockito
      .when(interactorRepository.findById(3L))
      .thenReturn(Optional.of(person));
    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.of(page));
    Mockito.when(pageService.isPageManagedByPerson(4L, 3L)).thenReturn(true);

    Interactor interactor = interactorService.getRepresentableInteractorByPerson(
      4L,
      3L
    );
    assertEquals(page, interactor);

    verify(pageService, times(1)).isPageManagedByPerson(4L, 3L);
  }

  @Test
  public void getRepresentableInteractorByPersonThrowsIfPageIsNotRepresentableByPerson() {
    Person person = new Person();
    person.setId(3L);
    Page page = new Page();
    page.setId(4L);

    Mockito
      .when(interactorRepository.findById(3L))
      .thenReturn(Optional.of(person));
    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.of(page));
    Mockito.when(pageService.isPageManagedByPerson(4L, 3L)).thenReturn(false);

    assertThrows(
      NotAuthorizedException.class,
      () -> {
        interactorService.getRepresentableInteractorByPerson(4L, 3L);
      }
    );
  }

  @Test
  public void getRepresentableInteractorsByPersonOnlyReturnsPersonIfAttemptingToInteractWithAnotherPerson() {
    Person person = new Person();
    Interactor interactingWith = new Person();

    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.of(person));
    Mockito
      .when(interactorRepository.findById(5L))
      .thenReturn(Optional.of(interactingWith));

    List<Interactor> result = interactorService.getRepresentableInteractorsByPerson(
      4L,
      5L
    );
    assertEquals(List.of(person), result);
  }

  @Test
  public void getRepresentableInteractorsByPersonOnlyReturnsPersonAndPageWhenPageIsManagedByPerson() {
    Person person = new Person();
    Interactor interactingWith = new Page();

    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.of(person));
    Mockito
      .when(interactorRepository.findById(5L))
      .thenReturn(Optional.of(interactingWith));
    Mockito.when(pageService.isPageManagedByPerson(5L, 4L)).thenReturn(true);

    List<Interactor> result = interactorService.getRepresentableInteractorsByPerson(
      4L,
      5L
    );
    assertEquals(List.of(person, interactingWith), result);
  }

  @Test
  public void getRepresentableInteractorsByPersonDefaultsToPersonPlusThePagesTheyManage() {
    Person person = new Person();
    Interactor interactingWith = new Page();

    Page foo = new Page();
    Page bar = new Page();
    Page baz = new Page();

    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.of(person));
    Mockito
      .when(interactorRepository.findById(5L))
      .thenReturn(Optional.of(interactingWith));
    Mockito.when(pageService.isPageManagedByPerson(5L, 4L)).thenReturn(false);
    Mockito
      .when(pageService.getAllPersonPages(4L))
      .thenReturn(List.of(foo, bar, baz));

    List<Interactor> result = interactorService.getRepresentableInteractorsByPerson(
      4L,
      5L
    );
    assertEquals(List.of(person, foo, bar, baz), result);
  }

  @Test
  public void getRepresentableInteractorsByPersonOnlyAdmitsPersonAsFirstParameter() {
    Page page = new Page();
    Mockito
      .when(interactorRepository.findById(4L))
      .thenReturn(Optional.of(page));

    assertThrows(
      ClassCastException.class,
      () -> {
        interactorService.getRepresentableInteractorsByPerson(4L, 5L);
      }
    );
  }
}
