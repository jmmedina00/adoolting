package io.github.jmmedina00.adoolting.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfiguration {
  @Value("${EMAIL_ADDRESS}")
  private String emailAddress;

  @Value("${EMAIL_PASSWORD}")
  private String emailPassword;

  @Value("${EMAIL_SENDER}")
  private String stmpServer;

  @Value("${EMAIL_SECURE}")
  private boolean isStmpServerSecure;

  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(stmpServer);
    mailSender.setPort(isStmpServerSecure ? 587 : 25);
    mailSender.setUsername(emailAddress);
    mailSender.setPassword(emailPassword);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put(
      "mail.smtp.starttls.enable",
      isStmpServerSecure ? "true" : "false"
    );

    return mailSender;
  }
}
