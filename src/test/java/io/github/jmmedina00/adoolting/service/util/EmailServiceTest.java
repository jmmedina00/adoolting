package io.github.jmmedina00.adoolting.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.entity.cache.simple.SimplePerson;
import io.github.jmmedina00.adoolting.repository.cache.EmailDataRepository;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import io.github.jmmedina00.adoolting.util.MethodDoesThatNameGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.expression.ThymeleafEvaluationContext;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayNameGeneration(MethodDoesThatNameGenerator.class)
public class EmailServiceTest {
  @MockBean
  private EmailDataRepository dataRepository;

  @MockBean
  private PersonLocaleConfigService localeConfigService;

  @MockBean
  private JavaMailSender emailSender;

  @MockBean
  private TemplateEngine templateEngine;

  @MockBean
  private JobScheduler jobScheduler;

  @Value("${EMAIL_ADDRESS}")
  private String sender;

  @Autowired
  private MessageSource messageSource; // Makes more sense not to mock this with resolvables in place

  @Autowired
  private EmailService emailService;

  @Captor
  private ArgumentCaptor<Context> contextCaptor;

  @Captor
  private ArgumentCaptor<MimeMessage> messageCaptor;

  @Test
  public void setUpEmailJobSavesDataWithRandomId() {
    UUID testUuid = UUID.nameUUIDFromBytes("Testing".getBytes());
    String uuidResult = testUuid.toString();
    EmailData data = new EmailData();
    data.setPerson(new SimplePerson());

    MockedStatic<UUID> uuidUtilities = Mockito.mockStatic(UUID.class);
    uuidUtilities.when(UUID::randomUUID).thenReturn(testUuid);

    emailService.setUpEmailJob(data, uuidResult);

    assertEquals(uuidResult, data.getId());
    verify(dataRepository, times(1)).save(data);

    uuidUtilities.closeOnDemand();
  }

  @Test
  public void prepareEmailDoesNothingIfDataCannotBeFoundInRepository()
    throws Exception {
    Mockito.when(dataRepository.findById("token")).thenReturn(Optional.empty());

    emailService.prepareEmail("token", "test");

    verify(localeConfigService, never()).getConfig(anyLong());
    verify(templateEngine, never()).process(anyString(), any());
    verify(emailSender, never()).send(any(MimeMessage.class));
  }

  @Test
  public void prepareEmailAssemblesAndSendsMessage() throws Exception {
    SimplePerson person = new SimplePerson();
    person.setId(6L);
    person.setFirstName("Juanmi");
    person.setEmail("juanmi@test.local");

    EmailData data = new EmailData();
    data.setPerson(person);
    data.setParameters(
      new HashMap<>(Map.of("theme", "foo", "coffee", "black"))
    );

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setLocale("es");
    config.setOffsetFromUTC(-60);

    Mockito
      .when(dataRepository.findById("token"))
      .thenReturn(Optional.of(data));
    Mockito.when(localeConfigService.getConfig(6L)).thenReturn(config);
    Mockito
      .when(templateEngine.process(eq("mail/template"), any()))
      .thenReturn("<p>Hola</p>");
    Mockito
      .when(emailSender.createMimeMessage())
      .thenReturn(
        new MimeMessage(Session.getDefaultInstance(new Properties()))
      );

    emailService.prepareEmail("token", "template");

    verify(templateEngine, times(1))
      .process(eq("mail/template"), contextCaptor.capture());
    Context context = contextCaptor.getValue();

    assertTrue(
      context.containsVariable(
        ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME
      )
    );
    assertEquals(-60, context.getVariable("utcOffset"));
    assertEquals(person, context.getVariable("person"));
    assertEquals("foo", context.getVariable("theme"));
    assertEquals("black", context.getVariable("coffee"));

    verify(emailSender, times(1)).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();

    assertEquals(sender, message.getFrom()[0].toString());
    assertEquals(
      "juanmi@test.local",
      message.getRecipients(Message.RecipientType.TO)[0].toString()
    );
    assertEquals("Sujeto", message.getSubject());
    assertEquals("<p>Hola</p>", message.getContent());
  }

  @Test
  public void prepareEmailAttachesSubjectAddendumToSubjectCode()
    throws Exception {
    SimplePerson person = new SimplePerson();
    person.setId(6L);
    person.setFirstName("Juanmi");
    person.setEmail("juanmi@test.local");

    EmailData data = new EmailData();
    data.setPerson(person);
    data.setParameters(
      new HashMap<>(Map.of("theme", "foo", "coffee", "black"))
    );
    data.setSubjectAddendum("specialty.special");

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setLocale("es");
    config.setOffsetFromUTC(-60);

    Mockito
      .when(dataRepository.findById("token"))
      .thenReturn(Optional.of(data));
    Mockito.when(localeConfigService.getConfig(6L)).thenReturn(config);
    Mockito
      .when(emailSender.createMimeMessage())
      .thenReturn(
        new MimeMessage(Session.getDefaultInstance(new Properties()))
      );

    emailService.prepareEmail("token", "template");

    verify(emailSender, times(1)).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertEquals("Special subject", message.getSubject());
  }

  @Test
  public void prepareEmailGetsSubjectUsingArgumentsFromData() throws Exception {
    SimplePerson person = new SimplePerson();
    person.setId(6L);
    person.setFirstName("Juanmi");
    person.setEmail("juanmi@test.local");

    EmailData data = new EmailData();
    data.setPerson(person);
    data.setParameters(
      new HashMap<>(Map.of("theme", "foo", "coffee", "black"))
    );
    data.setSubjectArguments(List.of("foo", "bar"));

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setLocale("es");
    config.setOffsetFromUTC(-60);

    Mockito
      .when(dataRepository.findById("token"))
      .thenReturn(Optional.of(data));
    Mockito.when(localeConfigService.getConfig(6L)).thenReturn(config);
    Mockito
      .when(emailSender.createMimeMessage())
      .thenReturn(
        new MimeMessage(Session.getDefaultInstance(new Properties()))
      );

    emailService.prepareEmail("token", "parammed");

    verify(emailSender, times(1)).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertEquals("foo is bar", message.getSubject());
  }

  @Test
  public void prepareEmailDefaultsToSimplerSubjectCodeWhenTemplatePlusAddendumCannotBeFound()
    throws Exception {
    SimplePerson person = new SimplePerson();
    person.setId(6L);
    person.setFirstName("Juanmi");
    person.setEmail("juanmi@test.local");

    EmailData data = new EmailData();
    data.setPerson(person);
    data.setParameters(
      new HashMap<>(Map.of("theme", "foo", "coffee", "black"))
    );
    data.setSubjectAddendum("non-existent");

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setLocale("es");
    config.setOffsetFromUTC(-60);

    Mockito
      .when(dataRepository.findById("token"))
      .thenReturn(Optional.of(data));
    Mockito.when(localeConfigService.getConfig(6L)).thenReturn(config);
    Mockito
      .when(emailSender.createMimeMessage())
      .thenReturn(
        new MimeMessage(Session.getDefaultInstance(new Properties()))
      );

    emailService.prepareEmail("token", "template");

    verify(emailSender, times(1)).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertEquals("Sujeto", message.getSubject());
  }

  @Test
  public void prepareEmailDefaultsToGenericMessageWhenTemplateCannotBeFound()
    throws Exception {
    SimplePerson person = new SimplePerson();
    person.setId(6L);
    person.setFirstName("Juanmi");
    person.setEmail("juanmi@test.local");

    String defaultMessage = messageSource.getMessage(
      "greeting",
      new String[] {  },
      Locale.forLanguageTag("es")
    );

    EmailData data = new EmailData();
    data.setPerson(person);
    data.setParameters(
      new HashMap<>(Map.of("theme", "foo", "coffee", "black"))
    );

    PersonLocaleConfig config = new PersonLocaleConfig();
    config.setLocale("es");
    config.setOffsetFromUTC(-60);

    Mockito
      .when(dataRepository.findById("token"))
      .thenReturn(Optional.of(data));
    Mockito.when(localeConfigService.getConfig(6L)).thenReturn(config);
    Mockito
      .when(emailSender.createMimeMessage())
      .thenReturn(
        new MimeMessage(Session.getDefaultInstance(new Properties()))
      );

    emailService.prepareEmail("token", "nonsense");

    verify(emailSender, times(1)).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertEquals(defaultMessage, message.getSubject());
  }
}
