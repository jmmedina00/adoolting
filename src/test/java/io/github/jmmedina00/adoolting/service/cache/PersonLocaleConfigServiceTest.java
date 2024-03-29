package io.github.jmmedina00.adoolting.service.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.repository.cache.PersonLocaleConfigRepository;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class PersonLocaleConfigServiceTest {
  @MockBean
  private PersonLocaleConfigRepository configRepository;

  @Autowired
  private PersonLocaleConfigService configService;

  MockedStatic<LocaleContextHolder> utilities;

  @BeforeEach
  public void initializeLocaleContext() {
    utilities = Mockito.mockStatic(LocaleContextHolder.class);
  }

  @AfterEach
  public void closeLocaleContext() {
    utilities.closeOnDemand();
  }

  @Test
  public void getConfigGetsConfigFromRepositoryByPersonId() {
    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setLocale("my-locale");
    config.setOffsetFromUTC(-60);

    Mockito.when(configRepository.findById(1L)).thenReturn(Optional.of(config));

    PersonLocaleConfig personConfig = configService.getConfig(1L);

    assertEquals(config.getLocale(), personConfig.getLocale());
    assertEquals(config.getOffsetFromUTC(), personConfig.getOffsetFromUTC());
    assertEquals(config, personConfig);
  }

  @Test
  public void getConfigReturnsFreshConfigIfNothingFoundInRepository() {
    Mockito.when(configRepository.findById(1L)).thenReturn(Optional.empty());

    PersonLocaleConfig config = configService.getConfig(1L);
    assertEquals(1L, config.getId());
    assertEquals(0, config.getOffsetFromUTC());
    assertNull(config.getLocale());
  }

  private static Stream<Arguments> localesAndExpectedOutcomes() {
    return List
      .of(
        Locale.CANADA,
        Locale.CANADA_FRENCH,
        Locale.US,
        Locale.UK,
        Locale.CHINA,
        Locale.ITALY,
        Locale.JAPAN
      )
      .stream()
      .map(locale -> Arguments.of(locale, locale.getLanguage()));
  }

  @ParameterizedTest
  @MethodSource("localesAndExpectedOutcomes")
  public void refreshForPersonOnlyChangesLocaleInConfig(
    Locale locale,
    String expectedOutcome
  ) {
    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setLocale("old");
    config.setOffsetFromUTC(-60);

    Mockito.when(configRepository.findById(1L)).thenReturn(Optional.of(config));
    Mockito
      .when(configRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));

    utilities.when(LocaleContextHolder::getLocale).thenReturn(locale);

    PersonLocaleConfig personConfig = configService.refreshForPerson(1L);
    assertEquals(-60, personConfig.getOffsetFromUTC());
    assertEquals(expectedOutcome, personConfig.getLocale());
  }

  @ParameterizedTest
  @MethodSource("localesAndExpectedOutcomes")
  public void updateUTCOffsetChangesBothOffsetAndCurrentLocale(
    Locale locale,
    String expectedOutcome
  ) {
    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setLocale("old");
    config.setOffsetFromUTC(-60);

    Mockito.when(configRepository.findById(1L)).thenReturn(Optional.of(config));
    Mockito
      .when(configRepository.save(any()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    utilities.when(LocaleContextHolder::getLocale).thenReturn(locale);

    PersonLocaleConfig personConfig = configService.updateUTCOffset(1L, 120);
    assertEquals(120, personConfig.getOffsetFromUTC());
    assertEquals(expectedOutcome, personConfig.getLocale());
  }
}
