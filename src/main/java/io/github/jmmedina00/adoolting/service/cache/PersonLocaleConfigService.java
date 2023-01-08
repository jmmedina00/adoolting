package io.github.jmmedina00.adoolting.service.cache;

import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.repository.cache.PersonLocaleConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PersonLocaleConfigService {
  @Autowired
  private PersonLocaleConfigRepository configRepository;

  private static final Logger logger = LoggerFactory.getLogger(
    PersonLocaleConfigService.class
  );

  public PersonLocaleConfig refreshForPerson(Long personId) {
    PersonLocaleConfig config = getConfig(personId);
    String localeString = LocaleContextHolder.getLocale().toString();
    config.setLocale(localeString);

    logger.info(
      "Changed locale for person {} to {}. UTC offset remains unchanged.",
      personId,
      localeString
    );
    return configRepository.save(config);
  }

  public PersonLocaleConfig updateUTCOffset(Long personId, int offsetFromUTC) {
    PersonLocaleConfig config = getConfig(personId);
    String localeString = LocaleContextHolder.getLocale().toString();
    config.setLocale(localeString);
    config.setOffsetFromUTC(offsetFromUTC);

    logger.info(
      "Changed locale for person {} to {} and UTC offset to {}.",
      personId,
      localeString,
      offsetFromUTC
    );
    return configRepository.save(config);
  }

  public PersonLocaleConfig getConfig(Long personId) {
    return configRepository
      .findById(personId)
      .orElseGet(
        () -> {
          PersonLocaleConfig config = new PersonLocaleConfig();
          config.setId(personId);
          return config;
        }
      );
  }
}
