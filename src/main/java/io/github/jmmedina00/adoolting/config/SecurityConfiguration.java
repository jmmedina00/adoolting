package io.github.jmmedina00.adoolting.config;

import java.util.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    http
      .authorizeHttpRequests(
        requests ->
          requests
            .antMatchers("/", "/restore-password/**")
            .permitAll()
            .anyRequest()
            .authenticated()
      )
      .formLogin()
      .loginPage("/")
      .loginProcessingUrl("/login")
      .failureHandler(authenticationFailureHandler())
      .defaultSuccessUrl("/home", true);
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
