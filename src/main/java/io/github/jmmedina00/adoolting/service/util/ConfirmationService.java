package io.github.jmmedina00.adoolting.service.util;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.ConfirmationToken;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import io.github.jmmedina00.adoolting.repository.util.ConfirmationTokenRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationService {
  @Autowired
  private ConfirmationTokenRepository tokenRepository;

  @Autowired
  private EmailService emailService;

  @Value("${confirmtoken.expires.hours}")
  private int expireInHours;

  public ConfirmationToken createTokenforPerson(Person person) {
    ConfirmationToken token = new ConfirmationToken();
    String actualToken = UUID.randomUUID().toString();
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.HOUR, expireInHours);
    Date expiresAt = calendar.getTime();

    token.setPerson(person);
    token.setToken(actualToken);
    token.setExpiresAt(expiresAt);

    ConfirmationToken saved = tokenRepository.save(token);
    emailService.setUpEmailJob(saved.getEmailData(), "confirm");
    return saved;
  }

  public ConfirmationToken confirmToken(String tokenStr)
    throws TokenExpiredException {
    ConfirmationToken token = tokenRepository.findByToken(tokenStr);

    if (token == null) {
      throw new TokenExpiredException();
    }

    Date expiredAt = token.getExpiresAt();
    Date now = new Date();

    if (expiredAt.before(now)) {
      throw new TokenExpiredException();
    }

    token.setConfirmedAt(now);
    return tokenRepository.save(token);
  }
}
