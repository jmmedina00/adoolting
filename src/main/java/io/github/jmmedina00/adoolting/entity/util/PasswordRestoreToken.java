package io.github.jmmedina00.adoolting.entity.util;

import io.github.jmmedina00.adoolting.entity.Person;
import java.util.Date;
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
public class PasswordRestoreToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String token;

  @ManyToOne
  @JoinColumn(name = "person_id")
  private Person person;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date expiresAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date usedAt;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Date expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Date getUsedAt() {
    return usedAt;
  }

  public void setUsedAt(Date usedAt) {
    this.usedAt = usedAt;
  }

  public Long getId() {
    return id;
  }
}
