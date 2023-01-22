package io.github.jmmedina00.adoolting.repository.person;

import io.github.jmmedina00.adoolting.entity.person.Person;
import org.springframework.data.jpa.domain.Specification;

public class PersonSpecs {

  public static Specification<Person> firstOrLastNameContains(String term) {
    String percented = "%" + term + "%";

    return (person, query, builder) ->
      builder.or(
        builder.like(person.get("firstName"), percented),
        builder.like(person.get("lastName"), percented)
      );
  }
}
