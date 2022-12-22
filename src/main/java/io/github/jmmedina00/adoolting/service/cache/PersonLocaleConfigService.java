package io.github.jmmedina00.adoolting.service.cache;

import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.repository.cache.PersonLocaleConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PersonLocaleConfigService {
  @Autowired
  private PersonLocaleConfigRepository configRepository;

  public PersonLocaleConfig refreshForPerson(Long personId) {
    PersonLocaleConfig config = getConfig(personId);
    config.setLocale(LocaleContextHolder.getLocale().toString());
    return configRepository.save(config);
  }

  public PersonLocaleConfig updateUTCOffset(Long personId, int offsetFromUTC) {
    PersonLocaleConfig config = getConfig(personId);
    config.setLocale(LocaleContextHolder.getLocale().toString());
    config.setOffsetFromUTC(offsetFromUTC);
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
