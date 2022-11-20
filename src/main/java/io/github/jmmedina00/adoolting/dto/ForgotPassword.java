package io.github.jmmedina00.adoolting.dto;

import java.io.Serializable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ForgotPassword implements Serializable {
  @Email(message = "{error.email}", regexp = "^.+@.+\\.[^\\.]{2,}$")
  @NotNull(message = "{error.email}")
  @NotEmpty(message = "{error.email}")
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
