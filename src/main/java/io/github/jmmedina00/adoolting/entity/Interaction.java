package io.github.jmmedina00.adoolting.entity;

import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import java.util.Date;
import java.util.List;
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
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Interaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "interactor_id")
  private Interactor interactor;

  @ManyToOne
  @JoinColumn(name = "receiver_interactor_id", nullable = true)
  private Interactor receiverInteractor;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date deletedAt;

  @OneToMany(mappedBy = "receiverInteraction", fetch = FetchType.LAZY)
  private List<Comment> comments;

  @OneToMany(mappedBy = "interaction", fetch = FetchType.LAZY)
  private List<Medium> media;

  public Long getId() {
    return id;
  }

  public Interactor getInteractor() {
    return interactor;
  }

  public void setInteractor(Interactor interactor) {
    this.interactor = interactor;
  }

  public Interactor getReceiverInteractor() {
    return receiverInteractor;
  }

  public void setReceiverInteractor(Interactor receiverInteractor) {
    this.receiverInteractor = receiverInteractor;
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

  public List<Comment> getComments() {
    return comments;
  }

  public List<Medium> getMedia() {
    return media;
  }

  public void setMedia(List<Medium> media) {
    this.media = media;
  }
}
