package io.github.jmmedina00.adoolting.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.ConfirmationToken;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import io.github.jmmedina00.adoolting.repository.util.ConfirmationTokenRepository;
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
public class ConfirmationServiceTest {
  @MockBean
  private ConfirmationTokenRepository tokenRepository;

  @MockBean
  private EmailService emailService;

  @Value("${confirmtoken.expires.hours}")
  private int expireInHours;

  @Autowired
  private ConfirmationService confirmationService;

  @Test
  public void createTokenforPersonCreatesTokenThatLastHoursSpecifiedAndEmailsIt() {
    Person person = new Person();
    UUID testUuid = UUID.nameUUIDFromBytes("Testing".getBytes());
    String uuidResult = testUuid.toString();
    Calendar fixedCalendar = Calendar.getInstance();
    fixedCalendar.setTime(new Date(1655085600000L)); // 2022/06/13 at 2:00AM UTC

    MockedStatic<Calendar> calendarUtilities = Mockito.mockStatic(
      Calendar.class
    );
    calendarUtilities.when(Calendar::getInstance).thenReturn(fixedCalendar);
    MockedStatic<UUID> uuidUtilities = Mockito.mockStatic(UUID.class);
    uuidUtilities.when(UUID::randomUUID).thenReturn(testUuid);

    Mockito
      .when(tokenRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ConfirmationToken token = confirmationService.createTokenforPerson(person);

    calendarUtilities.closeOnDemand();
    uuidUtilities.closeOnDemand();

    assertEquals(person, token.getPerson());
    assertEquals(uuidResult, token.getToken());
    assertEquals(
      fixedCalendar.getTime().getTime(),
      1655085600000L + (expireInHours * 3600_000)
    );

    verify(emailService, times(1)).setUpEmailJob(token, "confirm");
  }

  @Test
  public void confirmTokenSetsTokenConfirmedAtToNotNull()
    throws TokenExpiredException {
    ConfirmationToken token = new ConfirmationToken();
    token.setConfirmedAt(null);

    Mockito
      .when(tokenRepository.findToken("Token"))
      .thenReturn(Optional.of(token));
    Mockito
      .when(tokenRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    ConfirmationToken saved = confirmationService.confirmToken("Token");
    assertNotNull(saved.getConfirmedAt());
    assertEquals(token, saved);
  }

  @Test
  public void confirmTokenThrowsWhenTokenCannotBeFound() {
    Mockito
      .when(tokenRepository.findToken("Token"))
      .thenReturn(Optional.empty());

    assertThrows(
      TokenExpiredException.class,
      () -> {
        confirmationService.confirmToken("Token");
      }
    );
  }
}
