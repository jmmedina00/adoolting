package io.github.jmmedina00.adoolting.dto;

import io.github.jmmedina00.adoolting.entity.enums.Gender;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class PersonInfo implements Serializable {
  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String firstName;

  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String lastName;

  @NotNull(message = "{error.gender}")
  private Gender gender;

  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String about;

  private String status;

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

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
