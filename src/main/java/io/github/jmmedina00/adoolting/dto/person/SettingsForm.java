package io.github.jmmedina00.adoolting.dto.person;

import io.github.jmmedina00.adoolting.entity.enums.NotificationSetting;
import javax.validation.constraints.NotNull;

public class SettingsForm {
  @NotNull
  private boolean allowStrangersIntoProfile = true;

  @NotNull
  private boolean allowPostsAndCommentsFromStrangers = false;

  @NotNull
  private boolean allowInvitesFromStrangers = false;

  @NotNull
  private boolean acceptInvitesAutomatically = false;

  @NotNull
  private boolean emailOnConfirmables = true;

  @NotNull
  private NotificationSetting notifyIncomingEvents = NotificationSetting.EMAIL;

  @NotNull
  private NotificationSetting notifyComments = NotificationSetting.IN_APP;

  @NotNull
  private NotificationSetting notifyPostsFromOthers =
    NotificationSetting.IN_APP;

  @NotNull
  private NotificationSetting notifyActivityFromPages =
    NotificationSetting.EMAIL;

  @NotNull
  private NotificationSetting notifyPeoplesBirthdays = NotificationSetting.NONE;

  public boolean getAllowStrangersIntoProfile() {
    return allowStrangersIntoProfile;
  }

  public void setAllowStrangersIntoProfile(boolean allowStrangersIntoProfile) {
    this.allowStrangersIntoProfile = allowStrangersIntoProfile;
  }

  public boolean getAllowPostsAndCommentsFromStrangers() {
    return allowPostsAndCommentsFromStrangers;
  }

  public void setAllowPostsAndCommentsFromStrangers(
    boolean allowPostsAndCommentsFromStrangers
  ) {
    this.allowPostsAndCommentsFromStrangers =
      allowPostsAndCommentsFromStrangers;
  }

  public boolean getAllowInvitesFromStrangers() {
    return allowInvitesFromStrangers;
  }

  public void setAllowInvitesFromStrangers(boolean allowInvitesFromStrangers) {
    this.allowInvitesFromStrangers = allowInvitesFromStrangers;
  }

  public boolean getAcceptInvitesAutomatically() {
    return acceptInvitesAutomatically;
  }

  public void setAcceptInvitesAutomatically(
    boolean acceptInvitesAutomatically
  ) {
    this.acceptInvitesAutomatically = acceptInvitesAutomatically;
  }

  public boolean getEmailOnConfirmables() {
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
