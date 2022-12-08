package io.github.jmmedina00.adoolting.controller.common;

import io.github.jmmedina00.adoolting.entity.person.Person;
import io.github.jmmedina00.adoolting.entity.util.PersonDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticatedPerson {

  public static Person getPerson() {
    return (
      (PersonDetails) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal()
    ).getPerson();
  }

  public static Long getPersonId() {
    return getPerson().getId();
  }
}
