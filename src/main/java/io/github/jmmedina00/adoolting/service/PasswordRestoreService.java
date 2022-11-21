package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.entity.util.PasswordRestoreToken;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import io.github.jmmedina00.adoolting.repository.fromutil.PasswordRestoreTokenRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PasswordRestoreService {
  @Autowired
  PasswordRestoreTokenRepository restoreTokenRepository;

  @Autowired
  PersonService personService;

  @Autowired
  private EmailService emailService;

  @Autowired
  private JobScheduler jobScheduler;

  @Value("${restoretoken.expires.hours}")
  private int expireInHours;

  public PasswordRestoreToken createTokenFromEmail(String email)
    throws UsernameNotFoundException {
    PersonDetails personDetails = (PersonDetails) personService.loadUserByUsername(
      email
    );
    if (!personDetails.isEnabled()) {
      return null;
    }

    Person person = personDetails.getPerson();
    List<PasswordRestoreToken> stillValidTokens = person
      .getRestoreTokens()
      .stream()
      .filter(
        token ->
          token.getExpiresAt().after(new Date()) && token.getUsedAt() == null
      )
      .toList();

    if (stillValidTokens.size() > 0) {
      return null;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR, expireInHours);
    Date expiresAt = calendar.getTime();
    PasswordRestoreToken token = new PasswordRestoreToken();
    token.setPerson(person);
    token.setToken(UUID.randomUUID().toString());
    token.setExpiresAt(expiresAt);

    PasswordRestoreToken saved = restoreTokenRepository.save(token);
    jobScheduler.enqueue(
      () ->
        emailService.sendPasswordRestoreEmail(
          saved.getId(),
          LocaleContextHolder.getLocale()
        )
    );
    return saved;
  }
}
