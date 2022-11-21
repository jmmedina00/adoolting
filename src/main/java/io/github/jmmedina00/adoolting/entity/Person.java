package io.github.jmmedina00.adoolting.entity;

import io.github.jmmedina00.adoolting.entity.enums.Gender;
import io.github.jmmedina00.adoolting.entity.util.ConfirmationToken;
import io.github.jmmedina00.adoolting.entity.util.PasswordRestoreToken;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Person extends Interactor {
  @Column
  private String firstName;

  @Column
  private String lastName;

  @Column
  private String email;

  @Column
  private String password;

  @Temporal(TemporalType.DATE)
  @Column
  private Date birthDate;

  @Enumerated(EnumType.STRING)
  @Column
  private Gender gender;

  @OneToMany(mappedBy = "createdByPerson")
  private List<Page> createdPages;

  @OneToMany(mappedBy = "person")
  private List<PageManager> managedPages;

  @OneToMany(mappedBy = "person")
  private List<PersonStatus> statuses;

  @OneToMany(mappedBy = "fromPerson")
  private List<PrivateMessage> sentMessages;

  @OneToMany(mappedBy = "toPerson")
  private List<PrivateMessage> receivedMessages;

  @OneToOne(mappedBy = "person")
  private ConfirmationToken confirmationToken;

  @OneToMany(mappedBy = "person")
  private List<PasswordRestoreToken> restoreTokens;

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public ConfirmationToken getConfirmationToken() {
    return confirmationToken;
  }

  public List<Page> getCreatedPages() {
    return createdPages;
  }

  public List<PageManager> getManagedPages() {
    return managedPages;
  }

  public List<PersonStatus> getStatuses() {
    return statuses;
  }

  public List<PrivateMessage> getSentMessages() {
    return sentMessages;
  }

  public List<PrivateMessage> getReceivedMessages() {
    return receivedMessages;
  }

  public List<PasswordRestoreToken> getRestoreTokens() {
    return restoreTokens;
  }
}
