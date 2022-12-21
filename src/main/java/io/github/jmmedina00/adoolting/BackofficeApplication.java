package io.github.jmmedina00.adoolting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication(
  exclude = {
    SecurityAutoConfiguration.class,
    ManagementWebSecurityAutoConfiguration.class,
  }
)
@Profile("backoffice")
public class BackofficeApplication {

  public static void main(String[] args) {
    System.setProperty("spring.profiles.active", "backoffice");
    System.setProperty("spring.main.lazy-initialization", "true");
    System.setProperty("org.jobrunr.background-job-server.enabled", "false");
    System.setProperty("org.jobrunr.dashboard.enabled", "false");
    System.setProperty("server.port", "8081");

    SpringApplication.run(BackofficeApplication.class, args);
  }
}
