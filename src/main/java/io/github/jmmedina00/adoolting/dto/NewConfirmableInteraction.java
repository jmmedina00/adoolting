package io.github.jmmedina00.adoolting.dto;

import javax.validation.constraints.NotNull;

public class NewConfirmableInteraction {
  @NotNull
  private long personId;

  public long getPersonId() {
    return personId;
  }

  public void setPersonId(long personId) {
    this.personId = personId;
  }
}
