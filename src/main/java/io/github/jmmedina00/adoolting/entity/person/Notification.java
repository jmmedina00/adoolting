package io.github.jmmedina00.adoolting.entity.person;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.person.notification.CommentStrategy;
import io.github.jmmedina00.adoolting.entity.person.notification.ConfirmableStrategy;
import io.github.jmmedina00.adoolting.entity.person.notification.DataStrategy;
import io.github.jmmedina00.adoolting.entity.person.notification.InteractionStrategy;
import io.github.jmmedina00.adoolting.entity.person.notification.JoinRequestStrategy;
import io.github.jmmedina00.adoolting.entity.util.Emailable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Notification implements Emailable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "for_person_id")
  private Person forPerson;

  @ManyToOne
  @JoinColumn(name = "interaction_id")
  private Interaction interaction;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date readAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date deletedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Person getForPerson() {
    return forPerson;
  }

  public void setForPerson(Person forPerson) {
    this.forPerson = forPerson;
  }

  public Interaction getInteraction() {
    return interaction;
  }

  public void setInteraction(Interaction interaction) {
    this.interaction = interaction;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getReadAt() {
    return readAt;
  }

  public void setReadAt(Date readAt) {
    this.readAt = readAt;
  }

  public Date getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Date deletedAt) {
    this.deletedAt = deletedAt;
  }

  public boolean isActionable() { // TODO test/fix this method
    if (!(interaction instanceof ConfirmableInteraction)) {
      return false;
    }

    ConfirmableInteraction cInteraction = (ConfirmableInteraction) interaction;
    return Objects.equals(
      cInteraction.getReceiverInteractor().getId(),
      forPerson.getId()
    );
  }

  @Override
  public EmailData getEmailData() {
    DataStrategy strategy = new InteractionStrategy();
    if (interaction instanceof Comment) strategy = new CommentStrategy();
    if (interaction instanceof ConfirmableInteraction) strategy =
      new ConfirmableStrategy();
    if (interaction instanceof JoinRequest) strategy =
      new JoinRequestStrategy();

    return strategy.generateData(interaction, forPerson);
  }
}
