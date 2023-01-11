package io.github.jmmedina00.adoolting.service.util;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.cache.PersonLocaleConfig;
import io.github.jmmedina00.adoolting.entity.cache.simple.SimplePerson;
import io.github.jmmedina00.adoolting.entity.util.Emailable;
import io.github.jmmedina00.adoolting.repository.cache.EmailDataRepository;
import io.github.jmmedina00.adoolting.service.cache.PersonLocaleConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang3.LocaleUtils;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.expression.ThymeleafEvaluationContext;

@Service
public class EmailService {
  @Autowired
  private EmailDataRepository dataRepository;

  @Autowired
  private PersonLocaleConfigService localeConfigService;

  @Autowired
  private JavaMailSender emailSender;

  @Autowired
  private TemplateEngine templateEngine;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private JobScheduler jobScheduler;

  @Value("${EMAIL_ADDRESS}")
  private String sender;

  private static final Logger logger = LoggerFactory.getLogger(
    EmailService.class
  );

  public void setUpEmailJob(Emailable emailable, String template) {
    EmailData data = emailable.getEmailData();
    setUpEmailJob(data, template);
  }

  public void setUpEmailJob(EmailData data, String template) {
    String id = UUID.randomUUID().toString();
    data.setId(id);
    dataRepository.save(data);
    jobScheduler.enqueue(() -> prepareEmail(id, template));
    logger.info(
      "New email set up to person {} with template {}, data id: {}",
      data.getPerson().getId(),
      template,
      id
    );
  }

  @Job(name = "Send email")
  public void prepareEmail(String dataId, String template) throws Exception {
    EmailData data = dataRepository.findById(dataId).orElse(null);
    if (data == null) {
      logger.info(
        "Email with data {} not found. Assuming already sent",
        dataId
      );
      return;
    }

    SimplePerson person = data.getPerson();
    PersonLocaleConfig localeConfig = localeConfigService.getConfig(
      person.getId()
    );
    Locale locale = LocaleUtils.toLocale(
      Optional.ofNullable(localeConfig.getLocale()).orElse("en")
    );
    logger.debug(
      "Will send email {} with locale {}",
      dataId,
      locale.toString()
    );

    Context context = generateContext(locale);
    context.setVariable("utcOffset", localeConfig.getOffsetFromUTC());
    context.setVariable("person", person);

    for (Map.Entry<String, String> entry : data.getParameters().entrySet()) {
      context.setVariable(entry.getKey(), entry.getValue());
    }

    String contents = templateEngine.process("mail/" + template, context);

    ArrayList<String> subjectCodes = new ArrayList<>(
      List.of("email." + template, "greeting")
    );

    if (!data.getSubjectAddendum().isBlank()) {
      subjectCodes.add(
        0,
        "email." + template + "." + data.getSubjectAddendum()
      );
    }

    DefaultMessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(
      subjectCodes.toArray(new String[] {  }),
      data.getSubjectArguments().toArray()
    );

    String subject = applicationContext.getMessage(resolvable, locale);
    logger.debug(
      "Email {} subject is {}. Sending to {}",
      data.getId(),
      subject,
      person.getEmail()
    );

    sendEmail(person.getEmail(), subject, contents);

    logger.info(
      "Email {} has been sent successfully and will be deleted from cache",
      data.getId()
    );
    dataRepository.deleteById(dataId);
  }

  private Context generateContext(Locale locale) {
    Context context = new Context(locale);
    context.setVariable(
      ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
      new ThymeleafEvaluationContext(applicationContext, null)
    );

    return context;
  }

  private void sendEmail(String to, String subject, String contents)
    throws Exception {
    MimeMessage message = emailSender.createMimeMessage();
    message.setFrom(sender);
    message.setRecipients(Message.RecipientType.TO, to);
    message.setSubject(subject);
    message.setText(contents, "UTF-8", "html");
    emailSender.send(message);
  }
}
