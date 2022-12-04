package io.github.jmmedina00.adoolting.service.util;

import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.repository.cache.EmailDataRepository;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang3.LocaleUtils;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
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
  private JavaMailSender emailSender;

  @Autowired
  private TemplateEngine templateEngine;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private JobScheduler jobScheduler;

  @Value("${EMAIL_ADDRESS}")
  private String sender;

  public void setUpEmailJob(EmailData data, String template) {
    String id = UUID.randomUUID().toString();
    data.setId(id);
    dataRepository.save(data);
    jobScheduler.enqueue(() -> prepareEmail(id, template));
  }

  @Job(name = "Send email")
  public void prepareEmail(String dataId, String template) throws Exception {
    EmailData data = dataRepository.findById(dataId).orElse(null);
    if (data == null) {
      return; // Already sent
    }

    Locale locale = LocaleUtils.toLocale(data.getLocale());
    Context context = generateContext(locale);

    for (Map.Entry<String, String> entry : data.getParameters().entrySet()) {
      context.setVariable(entry.getKey(), entry.getValue());
    }

    String contents = templateEngine.process("mail/" + template, context);
    String subject = messageSource.getMessage(
      "email." + template,
      null,
      "Email",
      locale
    );
    sendEmail(data.getDestination(), subject, contents);
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
