package io.github.jmmedina00.adoolting.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class PrivateMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "TEXT")
  private String contents;

  @ManyToOne
  @JoinColumn(name = "from_person_id")
  private Person fromPerson;

  @ManyToOne
  @JoinColumn(name = "to_person_id")
  private Person toPerson;
}
