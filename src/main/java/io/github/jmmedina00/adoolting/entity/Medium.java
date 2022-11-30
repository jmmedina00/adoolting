package io.github.jmmedina00.adoolting.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Medium {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String reference;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date deletedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "interaction_id")
  private Interaction interaction;

  public Long getId() {
    return id;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Date deletedAt) {
    this.deletedAt = deletedAt;
  }

  public Interaction getInteraction() {
    return interaction;
  }

  public void setInteraction(Interaction interaction) {
    this.interaction = interaction;
  }
}
