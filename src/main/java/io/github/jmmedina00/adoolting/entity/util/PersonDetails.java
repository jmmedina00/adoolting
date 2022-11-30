package io.github.jmmedina00.adoolting.entity.util;

import io.github.jmmedina00.adoolting.entity.person.Person;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class PersonDetails implements UserDetails {
  private Person person;

  public PersonDetails(Person person) {
    this.person = person;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("USER"));
    return authorities;
  }

  @Override
  public String getPassword() {
    return person.getPassword();
  }

  @Override
  public String getUsername() {
    return person.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    ConfirmationToken token = person.getConfirmationToken();

    if (token == null) {
      return false;
    }

    return token.getConfirmedAt() != null;
  }

  public Person getPerson() {
    return person;
  }
}
