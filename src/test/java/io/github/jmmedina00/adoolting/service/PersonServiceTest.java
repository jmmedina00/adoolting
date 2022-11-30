package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.repository.PersonRepository;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.service.util.ConfirmationService;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonServiceTest {
  @MockBean
  private PersonRepository personRepository;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @MockBean
  private ConfirmationService confirmationService;

  @Autowired
  private PersonService personService;

  @Test
  public void getPersonByIdReturnsPerson() {
    Person expected = new Person();

    Mockito
      .when(personRepository.findById((long) 1))
      .thenReturn(Optional.of(expected));
    Assertions.assertEquals(expected, personService.getPerson((long) 1));
  }
}
