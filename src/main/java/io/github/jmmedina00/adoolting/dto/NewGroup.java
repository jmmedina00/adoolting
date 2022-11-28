package io.github.jmmedina00.adoolting.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class NewGroup {
  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String name;

  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
