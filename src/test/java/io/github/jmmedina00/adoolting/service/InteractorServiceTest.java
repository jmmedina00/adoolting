package io.github.jmmedina00.adoolting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jmmedina00.adoolting.entity.Interactor;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.InteractorRepository;
import io.github.jmmedina00.adoolting.service.page.PageService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
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
}
