package io.github.jmmedina00.adoolting.service.util;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PasswordRestoreToken;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import io.github.jmmedina00.adoolting.repository.util.PasswordRestoreTokenRepository;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import io.github.jmmedina00.adoolting.service.person.PersonService;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PasswordRestoreService {
  @Autowired
  private PasswordRestoreTokenRepository restoreTokenRepository;

  @Autowired
  private PersonService personService;

  @Autowired
  private PersonLocaleConfigService localeConfigService;

  @Autowired
  private EmailService emailService;

  @Value("${restoretoken.expires.hours}")
  private int expireInHours;

  public PasswordRestoreToken getToken(String token)
    throws TokenExpiredException {
    return restoreTokenRepository
      .findToken(token)
      .orElseThrow(TokenExpiredException::new);
  }

  public void changePasswordWithToken(String token, String newPassword)
    throws TokenExpiredException {
    PasswordRestoreToken tokenObj = getToken(token);
    personService.changePersonPassword(
      tokenObj.getPerson().getId(),
      newPassword
    );

    tokenObj.setUsedAt(new Date());
    restoreTokenRepository.save(tokenObj);
  }

  public PasswordRestoreToken createTokenFromEmail(String email)
    throws UsernameNotFoundException {
    PersonDetails personDetails = (PersonDetails) personService.loadUserByUsername(
      email
    );
    if (!personDetails.isEnabled()) {
      return null;
    }

    Person person = personDetails.getPerson();
    PasswordRestoreToken token = restoreTokenRepository
      .findTokenForPerson(person.getId())
      .orElseGet(() -> createNewToken(person));
    localeConfigService.refreshForPerson(person.getId());
    emailService.setUpEmailJob(token, "restore");
    return token;
  }

  private PasswordRestoreToken createNewToken(Person person) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR, expireInHours);
    Date expiresAt = calendar.getTime();
    PasswordRestoreToken token = new PasswordRestoreToken();
    token.setPerson(person);
    token.setToken(UUID.randomUUID().toString());
    token.setExpiresAt(expiresAt);
    return restoreTokenRepository.save(token);
  }
}
