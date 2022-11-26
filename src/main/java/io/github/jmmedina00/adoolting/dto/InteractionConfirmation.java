package io.github.jmmedina00.adoolting.dto;

import javax.validation.constraints.NotNull;

public class InteractionConfirmation {
  @NotNull
  private boolean isAccepted;

  @NotNull
  private boolean goToProfile;

  public boolean getIsAccepted() {
    return isAccepted;
  }

  public void setIsAccepted(boolean isAccepted) {
    this.isAccepted = isAccepted;
  }

  public boolean getGoToProfile() {
    return goToProfile;
  }

  public void setGoToProfile(boolean goToProfile) {
    this.goToProfile = goToProfile;
  }
}
