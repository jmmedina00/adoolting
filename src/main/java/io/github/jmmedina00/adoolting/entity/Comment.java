package io.github.jmmedina00.adoolting.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Comment extends Interaction {
  @ManyToOne
  @JoinColumn(name = "receiver_interaction_id")
  private Interaction receiverInteraction;

  @Column(columnDefinition = "TEXT")
  private String content;
}
