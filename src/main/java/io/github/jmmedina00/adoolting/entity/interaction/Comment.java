package io.github.jmmedina00.adoolting.entity.interaction;

import io.github.jmmedina00.adoolting.entity.Interaction;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Comment extends Interaction {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_interaction_id")
  private Interaction receiverInteraction;

  @Column(columnDefinition = "TEXT")
  private String content;

  public Interaction getReceiverInteraction() {
    return receiverInteraction;
  }

  public void setReceiverInteraction(Interaction receiverInteraction) {
    this.receiverInteraction = receiverInteraction;
  }

  public String getContent() {
    return content.trim();
  }

  public void setContent(String content) {
    this.content = content.trim();
  }
}
