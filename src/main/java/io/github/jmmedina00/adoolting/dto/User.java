package io.github.jmmedina00.adoolting.dto;

import io.github.jmmedina00.adoolting.dto.annotation.EmailMatches;
import io.github.jmmedina00.adoolting.dto.annotation.PasswordMatches;
import java.io.Serializable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

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
}
