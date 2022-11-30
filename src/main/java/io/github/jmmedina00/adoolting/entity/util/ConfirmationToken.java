package io.github.jmmedina00.adoolting.entity.util;

import io.github.jmmedina00.adoolting.entity.person.Person;
import java.util.Date;
import java.util.HashMap;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.context.i18n.LocaleContextHolder;

@Entity
public class ConfirmationToken implements Emailable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String token;

  @OneToOne
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
  private Date confirmedAt;

  public Long getId() {
    return id;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public Date getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Date expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Date getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(Date confirmedAt) {
    this.confirmedAt = confirmedAt;
  }

  @Override
  public EmailData getEmailData() {
    EmailData data = new EmailData(
      person.getEmail(),
      LocaleContextHolder.getLocale().toString()
    );
    HashMap<String, String> parameters = new HashMap<>();
    parameters.put("name", person.getFirstName());
    parameters.put("token", token);

    data.setParameters(parameters);
    return data;
  }
}
