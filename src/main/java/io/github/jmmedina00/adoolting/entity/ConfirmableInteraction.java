package io.github.jmmedina00.adoolting.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ConfirmableInteraction extends Interaction {
  // Friendship requests unless further extended

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date confirmedAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date ignoredAt;

  public Date getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(Date confirmedAt) {
    this.confirmedAt = confirmedAt;
  }

  public Date getIgnoredAt() {
    return ignoredAt;
  }

  public void setIgnoredAt(Date ignoredAt) {
    this.ignoredAt = ignoredAt;
  }
}
