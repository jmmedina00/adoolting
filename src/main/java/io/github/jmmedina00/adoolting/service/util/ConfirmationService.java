package io.github.jmmedina00.adoolting.service.util;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.ConfirmationToken;
import io.github.jmmedina00.adoolting.exception.TokenExpiredException;
import io.github.jmmedina00.adoolting.repository.util.ConfirmationTokenRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(
    ConfirmationService.class
  );

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
    logger.info(
      "Confirmation token has been created for person {}, id {}",
      person.getId(),
      saved.getId()
    );
    logger.debug("Token id {} value is {}", saved.getId(), saved.getToken());
    emailService.setUpEmailJob(saved, "confirm");
    return saved;
  }

  public ConfirmationToken confirmToken(String tokenStr)
    throws TokenExpiredException {
    ConfirmationToken token = tokenRepository
      .findToken(tokenStr)
      .orElseThrow(TokenExpiredException::new);
    token.setConfirmedAt(new Date());

    logger.info(
      "Token with id {} has been confirmed successfully",
      token.getId()
    );

    return tokenRepository.save(token);
  }
}
