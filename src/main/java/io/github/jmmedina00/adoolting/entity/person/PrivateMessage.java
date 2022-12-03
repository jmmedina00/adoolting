package io.github.jmmedina00.adoolting.entity.person;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.CreationTimestamp;

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

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  public Long getId() {
    return id;
  }

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public Person getFromPerson() {
    return fromPerson;
  }

  public void setFromPerson(Person fromPerson) {
    this.fromPerson = fromPerson;
  }

  public Person getToPerson() {
    return toPerson;
  }

  public void setToPerson(Person toPerson) {
    this.toPerson = toPerson;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }
}
