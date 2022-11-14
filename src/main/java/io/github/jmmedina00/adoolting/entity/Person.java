package io.github.jmmedina00.adoolting.entity;

import io.github.jmmedina00.adoolting.entity.enums.Gender;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
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
}
