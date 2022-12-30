package io.github.jmmedina00.adoolting.service.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.dto.PersonInfo;
import io.github.jmmedina00.adoolting.dto.User;
import io.github.jmmedina00.adoolting.dto.util.SecureDeletion;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.repository.PersonRepository;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import io.github.jmmedina00.adoolting.service.util.ConfirmationService;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonServiceTest {
  @MockBean
  private PersonRepository personRepository;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @MockBean
  private ConfirmationService confirmationService;

  @MockBean
  private PersonSettingsService settingsService;

  @MockBean
  private PersonLocaleConfigService localeConfigService;

  @MockBean
  private PersonStatusService statusService;

  @Autowired
  private PersonService personService;

  @Test
  public void getPersonByIdReturnsPerson() {
    Person expected = new Person();

    Mockito
      .when(personRepository.findActivePerson((long) 1))
      .thenReturn(Optional.of(expected));
    assertEquals(expected, personService.getPerson((long) 1));
  }

  @Test
  public void getPersonByBadIdThrowsException() {
    Mockito.when(personRepository.findActivePerson((long) 1)).thenThrow();
    assertThrows(
      Exception.class,
      () -> {
        personService.getPerson((long) 1);
      }
    );
  }

  @Test
  public void getPersonInfoReturnsEncapsulatedDataFromPerson() {
    Person returnedPerson = new Person();
    returnedPerson.setFirstName("Juanmi");
    returnedPerson.setLastName("Medina");
    returnedPerson.setGender(Gender.HE);
    returnedPerson.setAbout("Test");

    Mockito
      .when(personRepository.findActivePerson(any()))
      .thenReturn(Optional.of(returnedPerson));

    PersonInfo info = personService.getPersonInfo(1L);
    assertEquals(returnedPerson.getFirstName(), info.getFirstName());
    assertEquals(returnedPerson.getLastName(), info.getLastName());
    assertEquals(returnedPerson.getGender(), info.getGender());
    assertEquals(returnedPerson.getAbout(), info.getAbout());
  }

  @Test
  public void getPersonWithMatchingPasswordReturnsPersonAsIs()
    throws Exception {
    Person person = new Person();

    Mockito
      .when(personRepository.findActivePerson(any()))
      .thenReturn(Optional.of(person));
    Mockito.when(passwordEncoder.matches(any(), any())).thenReturn(true);

    Person returned = personService.getPersonWithMatchingPassword(
      1L,
      new SecureDeletion()
    );
    assertEquals(person, returned);
  }

  @Test
  public void getPersonWithMatchingPasswordThrowsBindExceptionWhenPasswordIsBad() {
    Person person = new Person();

    Mockito
      .when(personRepository.findActivePerson(any()))
      .thenReturn(Optional.of(person));
    Mockito.when(passwordEncoder.matches(any(), any())).thenReturn(false);

    SecureDeletion confirmation = new SecureDeletion();

    BindException e = assertThrows(
      BindException.class,
      () -> {
        personService.getPersonWithMatchingPassword(1L, confirmation);
      }
    );

    Optional<FieldError> interestingError = e
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .filter(
        error ->
          error.getField().equals("password") &&
          error.getCode().equals("error.password.incorrect")
      )
      .findFirst();
    assertTrue(interestingError.isPresent());
  }

  @Test
  public void updatePersonReturnsUpdatedPerson() {
    Person person = new Person();

    Mockito
      .when(personRepository.findActivePerson(any()))
      .thenReturn(Optional.of(person));
    Mockito
      .when(personRepository.save(any()))
      .thenAnswer(
        invocation -> {
          return invocation.getArgument(0);
        }
      );

    PersonInfo info = new PersonInfo();
    info.setFirstName("Juanmi");
    info.setLastName("Medina");
    info.setGender(Gender.HE);
    info.setAbout("Test");

    Person saved = personService.updatePerson(1L, info);
    assertEquals(info.getFirstName(), saved.getFirstName());
    assertEquals(info.getLastName(), saved.getLastName());
    assertEquals(info.getGender(), saved.getGender());
    assertEquals(info.getAbout(), saved.getAbout());
  }

  @Test
  public void updatePersonCallsStatusServiceForUpdate() {
    Person person = new Person();

    Mockito
      .when(personRepository.findActivePerson(any()))
      .thenReturn(Optional.of(person));
    Mockito
      .when(personRepository.save(any()))
      .thenAnswer(
        invocation -> {
          return invocation.getArgument(0);
        }
      );

    PersonInfo info = new PersonInfo();
    String status = "This is a status";
    info.setStatus(status);

    Person saved = personService.updatePerson(1L, info);
    verify(statusService, times(1)).updatePersonStatus(saved, status);
  }

  @Test
  public void changePersonPasswordEncodesPasswordBeforeSaving() {
    Person person = new Person();
    String newPassword = "mypassword";

    Mockito
      .when(personRepository.findActivePerson((long) 1))
      .thenReturn(Optional.of(person));
    Mockito
      .when(personRepository.save(any()))
      .thenAnswer(
        invocation -> {
          return invocation.getArgument(0);
        }
      );
    Mockito.when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");

    Person saved = personService.changePersonPassword((long) 1, newPassword);

    assertEquals(saved.getPassword(), "ENCODED");

    verify(passwordEncoder, times(1)).encode(newPassword);
    verify(personRepository, times(1)).findActivePerson((long) 1);
    verify(personRepository, times(1)).save(person);
  }

  @Test
  public void createPersonCreatesPersonFromUserData() throws BindException {
    String unusedEmail = "juanmi@juanmi.com";
    User user = new User(); // Service expects valid payload from controller
    user.setFirstName("Juanmi");
    user.setLastName("Medina");
    user.setEmail(unusedEmail);
    user.setConfirmEmail(unusedEmail);
    user.setPassword("123456");
    user.setConfirmPassword("123456");
    user.setGender(Gender.HE);
    user.setBirthday(new Date(950659200000L)); // 2000/02/16

    Mockito.when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
    Mockito.when(personRepository.findByEmail(unusedEmail)).thenReturn(null);
    Mockito
      .when(personRepository.save(any()))
      .thenAnswer(
        invocation -> {
          return invocation.getArgument(0);
        }
      );

    Person person = personService.createPersonFromUser(user);
    assertEquals(user.getFirstName(), person.getFirstName());
    assertEquals(user.getLastName(), person.getLastName());
    assertEquals(user.getEmail(), person.getEmail());
    assertEquals(user.getBirthday(), person.getBirthDate());
    assertEquals("ENCODED", person.getPassword());
    assertEquals(user.getGender(), person.getGender());

    verify(passwordEncoder, times(1)).encode(user.getPassword());
    verify(personRepository, times(1)).save(any());
  }

  @Test
  public void createPersonCallsAuxiliaryServices() throws BindException {
    Mockito
      .when(personRepository.save(any()))
      .thenAnswer(
        invocation -> {
          return invocation.getArgument(0);
        }
      );

    User user = new User();
    Person person = personService.createPersonFromUser(user);
    verify(confirmationService, times(1)).createTokenforPerson(person);
    verify(settingsService, times(1)).createSettingsForPerson(person);
    verify(localeConfigService, times(1)).refreshForPerson(any()); //
  }

  @Test
  public void createPersonWithUsedEmailResultsinException() {
    String usedEmail = "juanmi@juanmi.com";
    User user = new User(); // Service expects valid payload from controller
    user.setEmail(usedEmail);

    Mockito
      .when(personRepository.findByEmail(usedEmail))
      .thenReturn(new Person());

    assertThrows(
      BindException.class,
      () -> {
        personService.createPersonFromUser(user);
      }
    );
  }

  @Test
  public void loadUserByUsernameReturnsDetailsFromPersonEmail()
    throws UsernameNotFoundException {
    Person person = new Person();

    Mockito
      .when(personRepository.findByEmail("juanmi@juanmi.com"))
      .thenReturn(person);

    PersonDetails details = (PersonDetails) personService.loadUserByUsername(
      "juanmi@juanmi.com"
    );
    assertEquals(details.getPerson(), person);
  }

  @Test
  public void loadUserByUsernameThrowsCorrectExceptionWhenPersonNotFound() {
    Mockito
      .when(personRepository.findByEmail("juanmi@juanmi.com"))
      .thenReturn(null);

    assertThrows(
      UsernameNotFoundException.class,
      () -> {
        personService.loadUserByUsername("juanmi@juanmi.com");
      }
    );
  }
}
