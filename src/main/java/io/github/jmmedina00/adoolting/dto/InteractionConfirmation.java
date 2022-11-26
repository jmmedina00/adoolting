package io.github.jmmedina00.adoolting.dto;

import javax.validation.constraints.NotNull;

public class InteractionConfirmation {
  @NotNull
  private boolean isAccepted;

  public boolean getIsAccepted() {
    return isAccepted;
  }

  public void setIsAccepted(boolean isAccepted) {
    this.isAccepted = isAccepted;
  }
}
