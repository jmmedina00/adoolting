package io.github.jmmedina00.adoolting.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class PeopleGroup extends Interaction {
  @Column
  private String name;

  @Column
  private String description;

  @UpdateTimestamp
  private Date updatedAt;

  @OneToMany(mappedBy = "group")
  private List<JoinRequest> joinRequests;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public List<JoinRequest> getJoinRequests() {
    return joinRequests;
  }
}
