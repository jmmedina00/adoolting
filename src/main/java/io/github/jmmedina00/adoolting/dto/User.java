package io.github.jmmedina00.adoolting.dto;

import io.github.jmmedina00.adoolting.dto.annotation.EmailMatches;
import io.github.jmmedina00.adoolting.dto.annotation.HasMinimumAge;
import io.github.jmmedina00.adoolting.dto.annotation.PasswordMatches;
import io.github.jmmedina00.adoolting.dto.common.DateExtractOfDate;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EmailMatches
@PasswordMatches
public class User extends WithConfirmablePassword {
  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String firstName;

  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String lastName;

  // Additional check to ensure it's a somewhat valid domain
  @Email(message = "{error.email}", regexp = "^.+@.+\\.[^\\.]{2,}$")
  @NotNull(message = "{error.email}")
  @NotEmpty(message = "{error.email}")
  private String email;

  private String confirmEmail;

  @NotNull(message = "{error.gender}")
  private Gender gender;

  @NotEmpty
  @HasMinimumAge
  private DateExtractOfDate birthday;

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getConfirmEmail() {
    return confirmEmail;
  }

  public DateExtractOfDate getBirthday() {
    return birthday;
  }

  public Gender getGender() {
    return gender;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setConfirmEmail(String confirmEmail) {
    this.confirmEmail = confirmEmail;
  }

  public void setBirthday(DateExtractOfDate birthday) {
    this.birthday = birthday;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }
}
