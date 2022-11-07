package io.github.jmmedina00.adoolting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

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
            .antMatchers("/test/**")
            .authenticated()
            .antMatchers("/**")
            .permitAll()
      )
      .formLogin()
      .loginPage("/");
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails clara = User
      .withDefaultPasswordEncoder()
      .username("clara")
      .password("test")
      .roles("USER")
      .build();
    UserDetails mario = User
      .withDefaultPasswordEncoder()
      .username("mario")
      .password("1234")
      .roles("USER")
      .build();

    return new InMemoryUserDetailsManager(clara, mario);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
