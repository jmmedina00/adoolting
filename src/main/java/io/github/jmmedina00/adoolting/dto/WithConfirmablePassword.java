package io.github.jmmedina00.adoolting.dto;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public abstract class WithConfirmablePassword implements Serializable {
  @Length(min = 6, message = "{error.password}")
  @NotNull(message = "{error.password}")
  @NotEmpty(message = "{error.password}")
  private String password;

  private String confirmPassword;

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }
}
