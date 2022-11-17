package io.github.jmmedina00.adoolting.dto;

import io.github.jmmedina00.adoolting.dto.annotation.EmailMatches;
import io.github.jmmedina00.adoolting.dto.annotation.HasMinimumAge;
import io.github.jmmedina00.adoolting.dto.annotation.PasswordMatches;
import io.github.jmmedina00.adoolting.entity.enums.Gender;
import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@EmailMatches
@PasswordMatches
public class User implements Serializable {
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

  @Length(min = 6, message = "{error.password}")
  @NotNull(message = "{error.password}")
  @NotEmpty(message = "{error.password}")
  private String password;

  private String confirmPassword;

  @NotNull(message = "{error.gender}")
  private Gender gender;

  @DateTimeFormat(iso = ISO.DATE)
  @HasMinimumAge
  private Date birthday;

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

  public String getPassword() {
    return password;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public Date getBirthday() {
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

  public void setPassword(String password) {
    this.password = password;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }
}
