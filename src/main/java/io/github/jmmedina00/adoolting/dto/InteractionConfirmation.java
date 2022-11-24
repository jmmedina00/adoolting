package io.github.jmmedina00.adoolting.dto;

import javax.validation.constraints.NotNull;

public class InteractionConfirmation {
  @NotNull
  private boolean isAccepted;

  public boolean isAccepted() {
    return isAccepted;
  }

  public void setAccepted(boolean isAccepted) {
    this.isAccepted = isAccepted;
  }
}
