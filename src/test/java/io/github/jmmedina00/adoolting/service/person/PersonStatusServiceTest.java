package io.github.jmmedina00.adoolting.service.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.person.PersonStatus;
import io.github.jmmedina00.adoolting.repository.person.PersonStatusRepository;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
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
public class PersonStatusServiceTest {
  @MockBean
  private PersonStatusRepository statusRepository;

  @Autowired
  private PersonStatusService statusService;

  @Test
  public void updatePersonStatusCreatesNewStatusWithPersonAndContent() {
    String content = "Test";
    Person person = new Person();

    Mockito
      .when(statusRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    PersonStatus status = statusService.updatePersonStatus(person, content);

    assertEquals(person, status.getPerson());
    assertEquals(content, status.getContent());
  }

  @Test
  public void updatePersonStatusCreatesNoStatusIfContentIsEmpty() {
    String content = "";
    Person person = new Person();

    PersonStatus status = statusService.updatePersonStatus(person, content);

    assertNull(status);
    verify(statusRepository, never()).save(any());
  }
}
