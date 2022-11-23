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

  public boolean isTokenStillUseful(String token) {
    PasswordRestoreToken tokenObj = restoreTokenRepository.findByToken(token);

    if (tokenObj == null) {
      return false;
    }

    return (
      tokenObj.getExpiresAt().after(new Date()) && tokenObj.getUsedAt() == null
    );
  }

  public void changePasswordWithToken(String token, String newPassword) {
    PasswordRestoreToken tokenObj = restoreTokenRepository.findByToken(token);

    if (tokenObj == null) {
      return;
    }

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
      () -> emailService.prepareEmail(saved.getEmailData(), "restore")
    );
    return saved;
  }
}
