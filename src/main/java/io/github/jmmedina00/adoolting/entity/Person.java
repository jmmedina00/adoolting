package io.github.jmmedina00.adoolting.entity;

import io.github.jmmedina00.adoolting.entity.enums.Gender;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
}
