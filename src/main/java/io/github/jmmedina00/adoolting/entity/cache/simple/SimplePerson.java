package io.github.jmmedina00.adoolting.entity.cache.simple;

import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.person.Person;
import java.io.Serializable;

public class SimplePerson implements Serializable {
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private Gender gender;

  public SimplePerson(Person person) {
    setId(person.getId());
    setFirstName(person.getFirstName());
    setLastName(person.getLastName());
    setEmail(person.getEmail());
    setGender(person.getGender());
  }

  public SimplePerson() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }
}
