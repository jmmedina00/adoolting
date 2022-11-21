package io.github.jmmedina00.adoolting.service;

import io.github.jmmedina00.adoolting.entity.Person;
import io.github.jmmedina00.adoolting.entity.util.PasswordRestoreToken;
import io.github.jmmedina00.adoolting.repository.PersonRepository;
import io.github.jmmedina00.adoolting.repository.fromutil.PasswordRestoreTokenRepository;
import java.util.Locale;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import org.jobrunr.jobs.annotations.Job;
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
  private JavaMailSender emailSender;

  @Autowired
  private TemplateEngine templateEngine;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private PersonRepository personRepository;

  @Autowired
  private PasswordRestoreTokenRepository restoreTokenRepository; // TODO implement something better than this

  @Autowired
  private ApplicationContext applicationContext;

  @Value("${EMAIL_ADDRESS}")
  private String sender;

  @Job(name = "Password restore email")
  public void sendPasswordRestoreEmail(Long tokenId, Locale locale)
    throws Exception {
    PasswordRestoreToken tokenObj = restoreTokenRepository
      .findById(tokenId)
      .get();

    String token = tokenObj.getToken();
    String email = tokenObj.getPerson().getEmail();

    Context context = new Context(locale);
    context.setVariable(
      ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
      new ThymeleafEvaluationContext(applicationContext, null)
    );
    context.setVariable("token", token);

    String contents = templateEngine.process("mail/restore", context);
    sendEmail(email, "Restore password", contents);
  }

  @Job(name = "Confirmation email")
  public void sendConfirmationEmail(Long personId, Locale locale)
    throws Exception {
    Person person = personRepository.findById(personId).get();

    Context context = new Context(locale);
    context.setVariable(
      ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
      new ThymeleafEvaluationContext(applicationContext, null)
    );
    context.setVariable("name", person.getFirstName());
    context.setVariable("token", person.getConfirmationToken().getToken());

    String contents = templateEngine.process("mail/confirm", context);
    String subject = messageSource.getMessage("greeting", null, locale);

    sendEmail(person.getEmail(), subject, contents);
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
