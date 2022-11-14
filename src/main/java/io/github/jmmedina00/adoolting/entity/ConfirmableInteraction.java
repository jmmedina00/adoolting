package io.github.jmmedina00.adoolting.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ConfirmableInteraction extends Interaction {
  // Friendship requests unless further extended

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date confirmedAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date ignoredAt;
}
