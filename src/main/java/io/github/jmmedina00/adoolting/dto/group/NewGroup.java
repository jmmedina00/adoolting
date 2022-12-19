package io.github.jmmedina00.adoolting.dto.group;

import io.github.jmmedina00.adoolting.entity.enums.GroupAccessLevel;
import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class NewGroup implements Serializable {
  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String name;

  @NotNull(message = "{error.not_empty}")
  @NotEmpty(message = "{error.not_empty}")
  private String description;

  @NotNull
  private GroupAccessLevel accessLevel;

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

  public GroupAccessLevel getAccessLevel() {
    return accessLevel;
  }

  public void setAccessLevel(GroupAccessLevel accessLevel) {
    this.accessLevel = accessLevel;
  }
}
