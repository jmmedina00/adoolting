package io.github.jmmedina00.adoolting.config;

import io.github.jmmedina00.adoolting.config.handler.LocaleUpdaterSuccessHandler;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

@Configuration
@Profile("!backoffice")
@EnableWebSecurity
public class SecurityConfiguration {
  @Autowired
  private LocaleUpdaterSuccessHandler successHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    successHandler.setDefaultTargetUrl("/home");

    http
      .authorizeHttpRequests(
        requests ->
          requests
            .antMatchers(
              "/",
              "/restore-password/**",
              "/register",
              "/confirm/**"
            )
            .permitAll()
            .anyRequest()
            .authenticated()
      )
      .formLogin()
      .loginPage("/")
      .loginProcessingUrl("/login")
      .failureHandler(authenticationFailureHandler())
      .successHandler(successHandler);
    return http.build();
  }

  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler() {
    HashMap<String, String> urls = new HashMap<>();
    urls.put(
      "org.springframework.security.authentication.DisabledException",
      "/?disabled"
    );

    ExceptionMappingAuthenticationFailureHandler handler = new ExceptionMappingAuthenticationFailureHandler();
    handler.setExceptionMappings(urls);
    handler.setDefaultFailureUrl("/?error");
    return handler;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
