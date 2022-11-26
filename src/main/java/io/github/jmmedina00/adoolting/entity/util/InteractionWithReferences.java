package io.github.jmmedina00.adoolting.entity.util;

import io.github.jmmedina00.adoolting.entity.Interaction;
import java.util.List;

public class InteractionWithReferences {
  private Interaction interaction;
  private List<String> references;

  public Interaction getInteraction() {
    return interaction;
  }

  public void setInteraction(Interaction interaction) {
    this.interaction = interaction;
  }

  public List<String> getReferences() {
    return references;
  }

  public void setReferences(List<String> references) {
    this.references = references;
  }
}
