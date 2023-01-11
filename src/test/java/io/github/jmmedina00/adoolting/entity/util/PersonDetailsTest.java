package io.github.jmmedina00.adoolting.entity.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PersonDetailsTest {

  @Test
  public void getterMethodsReturnDataStraightFromPerson() {
    Person person = new Person();
    person.setEmail("juanmi@test.local");
    person.setPassword("MyPassword");

    PersonDetails details = new PersonDetails(person);
    assertEquals(person.getEmail(), details.getUsername());
    assertEquals(person.getPassword(), details.getPassword());
    assertEquals(person, details.getPerson());
  }

  @Test
  public void getAuthoritiesReturnsASimplePlaceholder() {
    Person person = new Person();
    PersonDetails details = new PersonDetails(person);

    assertEquals(
      List.of(new SimpleGrantedAuthority("USER")),
      details.getAuthorities()
    );
  }

  @Test
  public void isEnabledReturnsFalseIfThereIsNoConfirmationToken() {
    Person person = Mockito.mock(Person.class);
    Mockito.when(person.getConfirmationToken()).thenReturn(null);

    PersonDetails details = new PersonDetails(person);
    assertFalse(details.isEnabled());
  }

  @Test
  public void isEnabledReturnsFalseIfConfirmationTokenIsNotConfirmed() {
    ConfirmationToken token = new ConfirmationToken();
    token.setConfirmedAt(null);

    Person person = Mockito.mock(Person.class);
    Mockito.when(person.getConfirmationToken()).thenReturn(token);

    PersonDetails details = new PersonDetails(person);
    assertFalse(details.isEnabled());
  }

  @Test
  public void isEnabledReturnsTrueWhenConfirmationTokenIsConfirmed() {
    ConfirmationToken token = new ConfirmationToken();
    token.setConfirmedAt(new Date(500L));

    Person person = Mockito.mock(Person.class);
    Mockito.when(person.getConfirmationToken()).thenReturn(token);

    PersonDetails details = new PersonDetails(person);
    assertTrue(details.isEnabled());
  }
}
