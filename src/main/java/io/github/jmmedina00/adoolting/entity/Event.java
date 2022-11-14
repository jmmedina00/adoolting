package io.github.jmmedina00.adoolting.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Event extends PeopleGroup {
  @Column
  private String location; // TODO: make it a geometry

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  private Date happeningAt;
}
