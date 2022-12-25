package io.github.jmmedina00.adoolting.entity.person;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import io.github.jmmedina00.adoolting.entity.Interaction;
import io.github.jmmedina00.adoolting.entity.cache.EmailData;
import io.github.jmmedina00.adoolting.entity.group.JoinRequest;
import io.github.jmmedina00.adoolting.entity.interaction.Comment;
import io.github.jmmedina00.adoolting.entity.page.Page;
import io.github.jmmedina00.adoolting.entity.util.Emailable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

  public Date getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Date deletedAt) {
    this.deletedAt = deletedAt;
  }

  public boolean isActionable() {
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
    EmailData data = new EmailData();
    data.setPerson(forPerson);

    HashMap<String, String> parameters = new HashMap<>();
    parameters.put("interaction", interaction.getId().toString());
    data.setParameters(parameters);

    ArrayList<String> arguments = new ArrayList<>();
    arguments.add(interaction.getInteractor().getFullName());

    if (interaction instanceof Comment) {
      arguments.add(
        ((Comment) interaction).getReceiverInteraction()
          .getInteractor()
          .getFullName()
      );
    } else if (interaction.getReceiverInteractor() != null) {
      arguments.add(interaction.getReceiverInteractor().getFullName());
    }

    if (
      arguments.size() > 1 && !forPerson.getFullName().equals(arguments.get(1))
    ) {
      data.setSubjectAddendum("page");
    } else if (!(interaction.getInteractor() instanceof Page)) {
      data.setSubjectAddendum("profile");
    }

    if (interaction instanceof ConfirmableInteraction) {
      ConfirmableInteraction cInteraction = (ConfirmableInteraction) interaction;

      if (cInteraction.getConfirmedAt() != null) {
        Collections.reverse(arguments);
      }
      data.setSubjectAddendum("friend");
    }

    if (interaction instanceof JoinRequest) {
      JoinRequest joinRequest = (JoinRequest) interaction;
      arguments.add(joinRequest.getGroup().getName());
      String subjectAdd =
        "group" +
        (
          Objects.equals(
              interaction.getInteractor().getId(),
              joinRequest.getGroup().getInteractor().getId()
            )
            ? ".invite"
            : ".request"
        );

      data.setSubjectAddendum(subjectAdd);
    }

    data.setSubjectArguments(arguments);

    return data;
  }
}
