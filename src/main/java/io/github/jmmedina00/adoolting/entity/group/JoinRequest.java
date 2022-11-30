package io.github.jmmedina00.adoolting.entity.group;

import io.github.jmmedina00.adoolting.entity.ConfirmableInteraction;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class JoinRequest extends ConfirmableInteraction {
  @ManyToOne
  @JoinColumn(name = "group_id")
  private PeopleGroup group;

  public PeopleGroup getGroup() {
    return group;
  }

  public void setGroup(PeopleGroup group) {
    this.group = group;
  }
}
