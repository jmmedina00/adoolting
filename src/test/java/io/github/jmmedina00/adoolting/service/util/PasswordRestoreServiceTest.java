package io.github.jmmedina00.adoolting.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PasswordRestoreToken;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import io.github.jmmedina00.adoolting.repository.util.PasswordRestoreTokenRepository;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PasswordRestoreServiceTest {
  @MockBean
  private PasswordRestoreTokenRepository restoreTokenRepository;

  @MockBean
  private PersonService personService;

  @MockBean
  private PersonLocaleConfigService localeConfigService;

  @MockBean
  private EmailService emailService;

  @Value("${restoretoken.expires.hours}")
  private int expireInHours;

  @Autowired
  private PasswordRestoreService passwordRestoreService;

  @Test
  public void getTokenFetchesTokenFromRepository()
    throws TokenExpiredException {
    PasswordRestoreToken token = new PasswordRestoreToken();

    Mockito
      .when(restoreTokenRepository.findToken("token"))
      .thenReturn(Optional.of(token));

    PasswordRestoreToken obtained = passwordRestoreService.getToken("token");
    assertEquals(token, obtained);
  }

  public void getTokenThrowsIfTokenCannotBeFound() {
    Mockito
      .when(restoreTokenRepository.findToken("token"))
      .thenReturn(Optional.empty());

    assertThrows(
      TokenExpiredException.class,
      () -> {
        passwordRestoreService.getToken("token");
      }
    );
  }

  @Test
  public void changePasswordWithTokenCallsPersonServiceToChangePasswordThenDiscardsToken()
    throws TokenExpiredException {
    PasswordRestoreToken token = new PasswordRestoreToken();
    Person person = new Person();
    person.setId(6L);
    token.setPerson(person);
    Mockito
      .when(restoreTokenRepository.findToken("token"))
      .thenReturn(Optional.of(token));

    passwordRestoreService.changePasswordWithToken("token", "mynewpassword");

    assertNotNull(token.getUsedAt());

    verify(personService, times(1)).changePersonPassword(6L, "mynewpassword");
    verify(restoreTokenRepository, times(1)).save(token);
  }

  @Test
  public void changePasswordWithTokenThrowsIfTokenCannotBeFoundInRepository() {
    Mockito
      .when(restoreTokenRepository.findToken("token"))
      .thenReturn(Optional.empty());

    assertThrows(
      TokenExpiredException.class,
      () -> {
        passwordRestoreService.changePasswordWithToken(
          "token",
          "mynewpassword"
        );
      }
    );

    verify(personService, never()).changePersonPassword(6L, "mynewpassword");
    verify(restoreTokenRepository, never()).save(any());
  }

  @Test
  public void createTokenFromEmailGeneratesNewTokenSetsUpEmailAndRefreshesLocaleForPerson() {
    Calendar fixedCalendar = Calendar.getInstance();
    fixedCalendar.setTime(new Date(1655085600000L)); // 2022/06/13 at 2:00AM

    MockedStatic<Calendar> utilities = Mockito.mockStatic(Calendar.class);
    utilities.when(Calendar::getInstance).thenReturn(fixedCalendar);

    UUID testUuid = UUID.nameUUIDFromBytes("Testing".getBytes());
    String uuidResult = testUuid.toString();
    MockedStatic<UUID> uuidUtilities = Mockito.mockStatic(UUID.class);
    uuidUtilities.when(UUID::randomUUID).thenReturn(testUuid);

    Person person = new Person();
    person.setId(5L);

    PersonDetails details = Mockito.mock(PersonDetails.class);
    Mockito.when(details.getPerson()).thenReturn(person);
    Mockito.when(details.isEnabled()).thenReturn(true);

    Mockito
      .when(personService.loadUserByUsername("juanmi@test.local"))
      .thenReturn(details);
    Mockito
      .when(restoreTokenRepository.findTokenForPerson(5L))
      .thenReturn(Optional.empty());
    Mockito
      .when(restoreTokenRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    PasswordRestoreToken token = passwordRestoreService.createTokenFromEmail(
      "juanmi@test.local"
    );
    assertEquals(person, token.getPerson());
    assertEquals(uuidResult, token.getToken());
    assertEquals(
      1655085600000L + (expireInHours * 3600_000),
      token.getExpiresAt().getTime()
    );

    verify(localeConfigService, times(1)).refreshForPerson(5L);
    verify(emailService, times(1)).setUpEmailJob(token, "restore");

    utilities.closeOnDemand();
    uuidUtilities.closeOnDemand();
  }

  @Test
  public void createTokenFromEmailReturnsExistingTokenButStillCallsRefreshAndEmailJob() {
    PasswordRestoreToken token = new PasswordRestoreToken();

    Person person = new Person();
    person.setId(5L);

    PersonDetails details = Mockito.mock(PersonDetails.class);
    Mockito.when(details.getPerson()).thenReturn(person);
    Mockito.when(details.isEnabled()).thenReturn(true);

    Mockito
      .when(personService.loadUserByUsername("juanmi@test.local"))
      .thenReturn(details);
    Mockito
      .when(restoreTokenRepository.findTokenForPerson(5L))
      .thenReturn(Optional.of(token));

    PasswordRestoreToken returned = passwordRestoreService.createTokenFromEmail(
      "juanmi@test.local"
    );
    assertEquals(token, returned);

    verify(localeConfigService, times(1)).refreshForPerson(5L);
    verify(emailService, times(1)).setUpEmailJob(token, "restore");
  }

  @Test
  public void createTokenFromEmailReturnsNullIfPersonLoadedIsNotEnabled() {
    PersonDetails details = Mockito.mock(PersonDetails.class);
    Mockito.when(details.isEnabled()).thenReturn(false);

    Mockito
      .when(personService.loadUserByUsername("juanmi@test.local"))
      .thenReturn(details);
    PasswordRestoreToken token = passwordRestoreService.createTokenFromEmail(
      "juanmi@test.local"
    );
    assertNull(token);

    verify(localeConfigService, never()).refreshForPerson(anyLong());
    verify(emailService, never())
      .setUpEmailJob(any(PasswordRestoreToken.class), eq("restore"));
  }
}
