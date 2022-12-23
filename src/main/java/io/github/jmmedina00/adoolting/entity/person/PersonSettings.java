package io.github.jmmedina00.adoolting.entity.person;

import io.github.jmmedina00.adoolting.entity.enums.NotificationSetting;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class PersonSettings implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id")
  private Person person;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  @UpdateTimestamp
  private Date updatedAt;

  @Column
  private boolean allowStrangersIntoProfile = true;

  @Column
  private boolean allowPostsAndCommentsFromStrangers = false;

  @Column
  private boolean allowInvitesFromStrangers = false;

  @Column
  private boolean acceptInvitesAutomatically = false;

  @Column
  private boolean emailOnConfirmables = true;

  @Enumerated(EnumType.STRING)
  @Column
  private NotificationSetting notifyIncomingEvents = NotificationSetting.EMAIL;

  @Enumerated(EnumType.STRING)
  @Column
  private NotificationSetting notifyComments = NotificationSetting.IN_APP;

  @Enumerated(EnumType.STRING)
  @Column
  private NotificationSetting notifyPostsFromOthers =
    NotificationSetting.IN_APP;

  @Enumerated(EnumType.STRING)
  @Column
  private NotificationSetting notifyActivityFromPages =
    NotificationSetting.EMAIL;

  @Enumerated(EnumType.STRING)
  @Column
  private NotificationSetting notifyPeoplesBirthdays = NotificationSetting.NONE;

  public Long getId() {
    return id;
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

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public boolean isAllowStrangersIntoProfile() {
    return allowStrangersIntoProfile;
  }

  public void setAllowStrangersIntoProfile(boolean allowStrangersIntoProfile) {
    this.allowStrangersIntoProfile = allowStrangersIntoProfile;
  }

  public boolean isAllowPostsAndCommentsFromStrangers() {
    return allowPostsAndCommentsFromStrangers;
  }

  public void setAllowPostsAndCommentsFromStrangers(
    boolean allowPostsAndCommentsFromStrangers
  ) {
    this.allowPostsAndCommentsFromStrangers =
      allowPostsAndCommentsFromStrangers;
  }

  public boolean isAllowInvitesFromStrangers() {
    return allowInvitesFromStrangers;
  }

  public void setAllowInvitesFromStrangers(boolean allowInvitesFromStrangers) {
    this.allowInvitesFromStrangers = allowInvitesFromStrangers;
  }

  public boolean isAcceptInvitesAutomatically() {
    return acceptInvitesAutomatically;
  }

  public void setAcceptInvitesAutomatically(
    boolean acceptInvitesAutomatically
  ) {
    this.acceptInvitesAutomatically = acceptInvitesAutomatically;
  }

  public boolean isEmailOnConfirmables() {
    return emailOnConfirmables;
  }

  public void setEmailOnConfirmables(boolean emailOnConfirmables) {
    this.emailOnConfirmables = emailOnConfirmables;
  }

  public NotificationSetting getNotifyIncomingEvents() {
    return notifyIncomingEvents;
  }

  public void setNotifyIncomingEvents(
    NotificationSetting notifyIncomingEvents
  ) {
    this.notifyIncomingEvents = notifyIncomingEvents;
  }

  public NotificationSetting getNotifyComments() {
    return notifyComments;
  }

  public void setNotifyComments(NotificationSetting notifyComments) {
    this.notifyComments = notifyComments;
  }

  public NotificationSetting getNotifyPostsFromOthers() {
    return notifyPostsFromOthers;
  }

  public void setNotifyPostsFromOthers(
    NotificationSetting notifyPostsFromOthers
  ) {
    this.notifyPostsFromOthers = notifyPostsFromOthers;
  }

  public NotificationSetting getNotifyActivityFromPages() {
    return notifyActivityFromPages;
  }

  public void setNotifyActivityFromPages(
    NotificationSetting notifyActivityFromPages
  ) {
    this.notifyActivityFromPages = notifyActivityFromPages;
  }

  public NotificationSetting getNotifyPeoplesBirthdays() {
    return notifyPeoplesBirthdays;
  }

  public void setNotifyPeoplesBirthdays(
    NotificationSetting notifyPeoplesBirthdays
  ) {
    this.notifyPeoplesBirthdays = notifyPeoplesBirthdays;
  }
}
