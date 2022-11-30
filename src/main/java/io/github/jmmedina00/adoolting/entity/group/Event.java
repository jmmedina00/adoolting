package io.github.jmmedina00.adoolting.entity.group;

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

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Date getHappeningAt() {
    return happeningAt;
  }

  public void setHappeningAt(Date happeningAt) {
    this.happeningAt = happeningAt;
  }
}
